# CodeStats: Big Stats for Big Code

CodeStats is a framework for large scale queries over multiple code
bases. It exposes a simple but expressive query language,
automatically runs on a MapReduce cluster, and outputs statistics
on the prevalance of syntactic code patterns described by user-provided queries.

## Setup

### Requirements

CodeStats executes its queries using MapReduce, using bytecode stored
in HDFS.  You should setup a Hadoop cluster supporting at least MRv2
(i.e., with YARN support) and HDFS; instructions to do so are outside
of scope, but we strongly recommend using Cloudera Manager.

To run the build script, you will also need some version of
[Gradle](https://gradle.org/).

### Preparing the Environment

The programs that launch queries, upload bytecode to HDFS, etc. will
typically be run locally on your personal workstation/laptop/etc. and
will require that the Hadoop configuration is accessible locally. Make
sure you've downloaded the Hadoop xml configuration files to your
local machine and put them in an easily accessible folder.

You will also need to get the Hadoop jars for inclusion in the
classpath. The `copyDep` gradle action will copy the jars you need to
run any programs to the folder `$PROJECT/build/build-deps` where
`$PROJECT` is the folder containing this README. To run any programs
described here, your Java invocations will follow this pattern:

```
java -classpath '$PROJECT/build/build-deps:/path/to/etc/hadoop:$PROJECT/build/classes/java/main/' <mainclass> [args]
```

where `$PROJECT` is replaced by the path to the folder containing this
README, and `/path/to/etc/hadoop` is the path to the folder containing
`hdfs-site.xml`, `yarn-site.xml`, etc. For the rest of the README, assume all
relative paths are relative to the folder containing this README.

### Setting up HDFS

In HDFS, create the following directory structure:

```
/user/codestats/
/user/codestats/inputs/
/user/codestats/outputs/
/user/codestats/codestat-jars/
```

`inputs/` contains the sequence files that hold the bytecode of
your dataset, `outputs/` will contain the results of queries, and `codestat-jar/`
will contain the jars needed by the CodeStats runtime.

#### Preparing and Uploading the Runtime Jars

The CodeStats system requires the `Soot` library and its
dependencies. Unfortunately, the versions of ASM used by Soot and
Hadoop conflict, so you will have to work around this issue by using
[jarjar](https://github.com/shevek/jarjar) to rewrite the ASM
references in Soot. We have provided an `asm-rename.rule` file in the
repository to make this easy. Simply run:

```
java -jar /path/to/jarjar.jar process asm-rename.rule /path/to/asm-debug-all-5.1.jar asm-rename.jar
java -jar /path/to/jarjar.jar process asm-rename.rule /path/to/soot-3.0.0-SNAPSHOT.jar soot-rename.jar
```

`asm-rename.jar` and `soot-rename.jar` will be the jars you upload to HDFS, do *not* upload
the original versions of `soot-3.0.0-SNAPSHOT` or `asm-debug-all-5.1.jar`.

After renaming, upload `asm-rename.jar`, `soot-rename.jar`, and the dependency jars of `soot` to the folder
`/user/codestats/codestat-jars`. To discover the dependencies of `soot`, use the gradle task `dependencies`
and find the listing for `ca.mcgill.sable:soot:3.0.0-SNAPSHOT`. To acquire the dependency jars, use the `copyDep`
task mentioned above and look in the `build/build-deps` folder.

## Building

To build codestats, simply run `gradle compileJava`.

## Creating the Evaluation Suite

CodeStats operates over SequenceFile, where each SequenceFile contains
all of the classes in an application in bytecode form. More specifically,
each SequenceFile is a sequence of key/value pairs, where the key is the fully-qualified
name of a class, and the value is a ByteSequence containing the bytecode for the
class named by the key.

To create these SequenceFiles, use the
`edu.washington.cse.codestats.runner.SequenceWriter` utility: this program
traverses a directory or jar file, putting all classes encountered
into a single SequenceFile. The invoke the utility, use the command line template
described in `Preparing the Environment`, with `edu.washington.cse.codestats.SequenceWriter`
as the `<mainclass>` and with the arguments `/path/to/folder-or-jar /user/codestats/inputs/some/path`
where `/path/to/folder-or-jar` is the path to a folder or jar file on your _local_ filesystem,
and `/user/codestats/inputs/some/path` is some path located in the `/user/codestats/inputs` folder
in HDFS. If it helps organizationally, subfolders may be used within `/user/codestats/inputs`.

For example, you could use the SequenceWriter utility as follows:

```
java -classpath '...' edu.washington.cse.codestats.runner.SequenceWriter ~/libs/guava-14.0.jar /user/codestats/inputs/guava-14.0
```

## Running Queries

Once you have added whatever libraries/applications/etc. you wish to
the CodeStats input set, you can run queries over the dataset. To execute a query,
use the `edu.washington.cse.codestats.runner.Runner` program. The `Runner` is used as follows:

```
java -classpath '...' edu.washington.cse.codestats.runner.Runner <query-name> <query-text>
```

where `query-name` is a descriptive name of the query, and
`query-text` is the query written in the CodeStats Query Language
described below. Alternatively, if `query-text` is a literal `-` the query
is read from STDIN.

After the query has successfully completed, the statistics computed by the query will
be found in `/user/codestats/output/<query-name>/part-r-00000` where `query-name` is the name
given when invoking the Runner.

### Understanding CodeStats output

CodeStats queries count the occurrences of syntactic patterns, so all
CodeStats output will be a list of counts. For each pattern defined in
the input query, there will be a line in the output text file giving
the count of items that matched that pattern. For example, if you define
a pattern over expressions named `null_args`, and codestats found 1000 expressions that matched
this pattern, then the output file will contain the line:

```
null_args 1000
```

There are a handful of statistics included in the output that describe the query execution. `$NUM_METHODS` is the
total number of methods processed during query execution. `$FAILED_METHODS` is the number of methods that
generated an exception during processing. This number will likely be non-zero due to underlying bugs in the
Soot framework, and should be ignored if the ration of `$FAILED_METHODS` to `$NUM_METHODS` is sufficiently low.
The error behavior during execution is described below. Finally, `$FAILED_CLASSES` is the number
of classes that failed to load during query execution. This number will like be non-zero, but can
likely be ignored if it is sufficiently small, i.e., <10.

## Query Language

The CodeStats Query Language is described by the following BNF grammar:

_Program_ ::= _Query_* <br/>
_Query_ ::= _Ident_ `:` _Mode_ _Syntax_ _Ident_ (`within` _Ident_)? `where` `{` _Conjunctive_ `}` <br/>
_Mode_ ::= `exists` | `count` | `hybrid` <br/>
_Syntax_ ::= `expression` | `statement` <br/>
_Conjunctive_ ::= _Disjunctive_ (`and` _Disjunctive_)* <br/>
_Disjunctive_ ::= _Atomic_ (`or` _Atomic_) <br/>
_Atomic_ ::= _Attribute_ `is` `not`? (`constant`|`static`|`local`|`null`|`this`|`arg`) | <br/>
 | _Attribute_ _Compare_ _Literal_ <br/>
 | `(` _Conjunctive_ `)` <br/>
_Attribute_ ::= _Ident_(`.`_Ident_(`[`_Index_`]`)?)* <br/>
_Index_ ::= `*` | `?` | 0 | 1 | ... <br/>
_Compare_ ::= `!=` | `==` | `<` | `>` | `<=`| `>=` <br/>
_Literal_ ::= _Number_ | _String_

where _Number_ are integer literals, and _String_ are single quoted strings (no escaping).

A program is a sequence of query definitions, each of which names and
describes an individual statistic to compute. The query language only
supports predicated counting statistics, which tally the syntactic
entities satisfying a predicate. The language supports predicates over
two sorts of syntactic entities: expressions and statements. The count
mode indicates a direct count of successful matches. For a bit greater
fiexibility, the `exists` mode limits the number of matches per method
to one; in other words, the exists mode enacts a tally of methods that
contain at least one expression/statement satisfying the
predicate. A `hybrid` query is syntactic sugar for one `exists`
and one `count` query with the same predicate _P_.
Lastly, a query definition may also refine another query
definition using a within clause; the semantic meaning is as if the
the refined query definition's predicate were conjoined before the
current one’s predicate.

A query predicate is a collection of atomic
predicates combined together with Boolean conjunction and
disjunction. An atomic predicate asserts a simple property on
attributes of the expression or statement. Depending on the kind of
expression or statement, valid attributes may include the kind of
expression or statement, the list of argument/operand expressions, the
receiver expression, etc.  Trait checks (i.e., checks for `constant`,
`local`, and `null`) are supported for all expression forms; the only
exception is `static`, which is used to check whether a `field` or
`method` is declared to be static or not (see below).

The first identifier in a query (i.e., before the colon) is the name
of the query. After declaring the _mode_ and _syntax_, the second _Ident_
gives the symbolic name of the expression or statement whose attributes
and traits are checked in the predicate of the query. For example,
in the query `foo: count expression e where { ... }`, all attribute
accesses must be rooted at the identified `e`.

### Attributes

The following table briefly summarizes the supported attributes in
CodeStats. Essentially, attributes expose the interface of the Java
abstract syntax tree (AST) rooted at the expression or
statement. Since different expressions/statements may have difeerent
AST structure, accessing an invalid attribute falsifies the atomic
predicate. The attributes kind and host are present on expressions and
statements of any kind.  To allow syntactic patterns that depend on
the particular kind of expression or statement, query predicates are
always evaluated with Boolean short-circuit semantics. In the following, the name of the attribute
is followed by the type in parentheses, each row gives the supported attributes for a type. A
type name suffixed with `[]` indicates the attribute is an array of the given type.

| **type** | **Attributes** |
| --- | --- |
| `expr` | `kind` (String), `type` (String), `host` (`method`) |
| `method` | `declaringClass` (String), `returnType` (String), `paramTypes` (String[]), `name` (String), `signature` (String) |
| `binop` | `operands` (`expr`[]), `lop` (`expr`), `rop` (`value`) |
| `unop` | `operand` (`expr`) |
| `cast_expr` | `cast_type` (String), `castee` (`expr`) |
| `field_ref` | `field` (`field`) |
| `field` | `name` (String), `type` (String), `declaringClass` (String) |
| `instance_fieldref` | `base_ptr` (`expr`) |
| `method_call` | `method` (`method`), `args` (`expr`[]) |
| `instance_method_call` | `receiver` (`expr`)
| `array_ref` | `index` (`expr`), `array` (`expr`) |
| `new_array` | `size` (`expr`), `baseType` (String) |
| `alloc` | `allocType` (String), `constrArgs` (`expr`[])
| `stmt` | `kind` (String), `host` (`method`) |
| `invoke_stmt` | `method_call` (`method_call`) |
| `assign_stmt` | `lhs` (`expr`), `rhs` (`expr`) |
| `return_stmt` | `ret_val` (`expr`) |

The `kind` attribute defined on the `stmt` and `expr` indicate the precise runtime
type of the statement or expression. This correspondence is given by the following tables:

| `expr.kind` is... | ... runtime type is... |
| --- | --- |
| `ArrayRead` | `array_ref` |
| `InstanceField` | `instance_fieldref` |
| `StaticField` | `field_ref` |
| `New` | `alloc` |
| `Binop` | `binop` |
| `Cast` | `cast_expr` |
| `Unop` | `unop` |
| `StaticInvoke` | `method_call` |
| `InstanceInvoke` | `instance_method_call` |
| `Constant` | `expr` |
| `NewArray` | `new_array` |

| `stmt.kind` is... | ... runtime type is ... |
| --- | --- |
| `Assign` | `assign_stmt` |
| `Invoke` | `invoke_stmt` |
| `Return` | `return_stmt` |

Finally, some of the types are in a subtyping relation. If a type _t_ is a subtype of _u_, and
_u_ has some attribute _attr_, then _t_ also has attribute _attr_. The subtyping relation is given as:

| Type(s) ... | ... are subtypes of... |
| --- | --- |
| `method_call`, `binop`, `unop`, `fieldref`, `array_ref`, `alloc`, `new_array`, `cast_expr` | `value` |
| `instance_fieldref` | `fieldref` |
| `instance_method_call` | `method_call` |
| `assign_stmt`, `invoke_stmt`, `return_stmt` | `stmt` |

#### Types

Several of the attributes given above refer to static types (e.g.,
`expr.type`, `cast_expr.cast_type`, etc.).  These attributes are of
type string, and their value is the Soot's string representation of
the static type. For example, the string representation of an array of integers
is `int[]`, and an array of Strings is `java.lang.String[]`. Only exact
matching against the string representation is supported, no subtype
checking is performed when comparing against types.

#### Arrays

Array attributes support subscripting and retrieving the length of the array. Given an
attribute _a_ yielding an array, the length can be accessed with _a_.`length`. Arrays may
be subscripted using numbers, which the obvious interpretation, or with the special subscripts
`*` and `?`. `*` indicates that all elements in the array must satisfy the remainder of the attribute
check, whereas `?` indicates that at least one element must satisfy the remainder of the attributes.
More precisely, suppose we have _a[*].b > n_, where _a_ and _b_ are arbitrary sequences of attributes.
This is equivalent to:

```
if(a.length == 0) {
  return false;
}
foreach(x in a) {
  if(!(x.b > n)) {
    return false;
  }
}
return true;
```

That is, for each element _x_ in the non-empty array given by the attribute
sequence _a_, the attribute sequence _x.b_ must be greater than _n_. `?` has a similar
interpretation except the loop returns true if any _x_ satisfies the predicate. When
interpreting `?` and `*`, empty arrays are assumed to be false.

## Error Handling

If an exception is thrown when processing a method, no statistics are emitted for any expressions
or statements appearing in that method. In other words, either every expression or statement
must be successfully checked against the patterns described in the input query, or else no statements
or expressions in the methods will count towards the final total.
