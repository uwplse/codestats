package edu.washington.cse.codestats;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import edu.washington.cse.codestats.PredicateAtom.Type;
import edu.washington.cse.codestats.PredicateMirror.PredicateType;
import fj.F2;

public class Compiler {
	private static final Table<String, String, Translator> ATTR = HashBasedTable.create();
	private static final Table<String, String, Translator> TRAITS = HashBasedTable.create();
	private static final Table<String, String, String> AVAIL_ATTR = HashBasedTable.create();
	private static final Table<String, String, String> AVAIL_TRAITS = HashBasedTable.create();
	private static final Map<String, String> TYPE_TABLE = new HashMap<>();

	static {
		TYPE_TABLE.put("STRING", "String");

		// START AUTO GENERATED CODE
		ATTR.put("unop", "operand", new InlineTranslator("{0}.getOp()", "value"));
		TYPE_TABLE.put("unop", "soot.jimple.internal.AbstractUnopExpr");
		TRAITS.put("unop", "null", new InlineTranslator("{0} instanceof soot.jimple.NullConstant"));
		TRAITS.put("unop", "constant", new InlineTranslator("{0} instanceof soot.jimple.Constant"));
		TRAITS.put("unop", "local", new InlineTranslator("{0} instanceof soot.Local"));
		ATTR.put("method_call", "args", new InlineTranslator("{0}.getArgs()", "value"));
		ATTR.put("method_call", "method", new InlineTranslator("{0}.getMethodRef()", "method"));
		TYPE_TABLE.put("method_call", "soot.jimple.InvokeExpr");
		TRAITS.put("method_call", "null", new InlineTranslator("{0} instanceof soot.jimple.NullConstant"));
		TRAITS.put("method_call", "constant", new InlineTranslator("{0} instanceof soot.jimple.Constant"));
		TRAITS.put("method_call", "local", new InlineTranslator("{0} instanceof soot.Local"));
		ATTR.put("array_ref", "index", new InlineTranslator("{0}.getIndex()", "value"));
		ATTR.put("array_ref", "array", new InlineTranslator("{0}.getBase()", "value"));
		TYPE_TABLE.put("array_ref", "soot.jimple.ArrayRef");
		TRAITS.put("array_ref", "null", new InlineTranslator("{0} instanceof soot.jimple.NullConstant"));
		TRAITS.put("array_ref", "constant", new InlineTranslator("{0} instanceof soot.jimple.Constant"));
		TRAITS.put("array_ref", "local", new InlineTranslator("{0} instanceof soot.Local"));
		ATTR.put("binop", "operands", new InlineTranslator("CountUtil.listOf({0}.getOp1(), {0}.getOp2())", "value"));
		ATTR.put("binop", "lop", new InlineTranslator("{0}.getOp1()", "value"));
		ATTR.put("binop", "rop", new InlineTranslator("{0}.getOp2()", "value"));
		TYPE_TABLE.put("binop", "soot.jimple.internal.AbstractBinopExpr");
		TRAITS.put("binop", "null", new InlineTranslator("{0} instanceof soot.jimple.NullConstant"));
		TRAITS.put("binop", "constant", new InlineTranslator("{0} instanceof soot.jimple.Constant"));
		TRAITS.put("binop", "local", new InlineTranslator("{0} instanceof soot.Local"));
		ATTR.put("alloc", "allocType", new InlineTranslator("{0}.getBaseType().getClassName()", "STRING"));
		ATTR.put("alloc", "constrArgs", new InlineTranslator("{0}.getArgs()", "value"));
		TYPE_TABLE.put("alloc", "soot.grimp.internal.GNewInvokeExpr");
		TRAITS.put("alloc", "null", new InlineTranslator("{0} instanceof soot.jimple.NullConstant"));
		TRAITS.put("alloc", "constant", new InlineTranslator("{0} instanceof soot.jimple.Constant"));
		TRAITS.put("alloc", "local", new InlineTranslator("{0} instanceof soot.Local"));
		ATTR.put("stmt", "kind", new BlockTranslator("soot.Stmt", "String", "// $BLOCK$\nif({0} instanceof soot.jimple.AssignStmt) {\n   return \"Assign\";\n} " +
				"else if({0} instanceof soot.jimple.InvokeStmt) {\n   return \"Invoke\";\n} " +
				"else {\n   return \"Other\";\n}\n", "STRING"));
		TYPE_TABLE.put("stmt", "soot.jimple.Stmt");
		ATTR.put("new_array", "size", new InlineTranslator("{0}.getSize()", "value"));
		ATTR.put("new_array", "baseType", new InlineTranslator("{0}.getBaseType().toString()", "STRING"));
		TYPE_TABLE.put("new_array", "soot.jimple.NewArrayExpr");
		TRAITS.put("new_array", "null", new InlineTranslator("{0} instanceof soot.jimple.NullConstant"));
		TRAITS.put("new_array", "constant", new InlineTranslator("{0} instanceof soot.jimple.Constant"));
		TRAITS.put("new_array", "local", new InlineTranslator("{0} instanceof soot.Local"));
		ATTR.put("instance_fieldref", "base_ptr", new InlineTranslator("{0}.getBase()", "value"));
		TYPE_TABLE.put("instance_fieldref", "soot.jimple.InstanceFieldRef");
		TRAITS.put("instance_fieldref", "null", new InlineTranslator("{0} instanceof soot.jimple.NullConstant"));
		TRAITS.put("instance_fieldref", "constant", new InlineTranslator("{0} instanceof soot.jimple.Constant"));
		TRAITS.put("instance_fieldref", "local", new InlineTranslator("{0} instanceof soot.Local"));
		ATTR.put("instance_method_call", "receiver", new InlineTranslator("{0}.getBase()", "value"));
		TYPE_TABLE.put("instance_method_call", "soot.jimple.InstanceInvokeExpr");
		TRAITS.put("instance_method_call", "null", new InlineTranslator("{0} instanceof soot.jimple.NullConstant"));
		TRAITS.put("instance_method_call", "constant", new InlineTranslator("{0} instanceof soot.jimple.Constant"));
		TRAITS.put("instance_method_call", "local", new InlineTranslator("{0} instanceof soot.Local"));
		ATTR.put("fieldref", "field", new InlineTranslator("{0}.getFieldRef()", "field"));
		TYPE_TABLE.put("fieldref", "soot.jimple.FieldRef");
		TRAITS.put("fieldref", "null", new InlineTranslator("{0} instanceof soot.jimple.NullConstant"));
		TRAITS.put("fieldref", "constant", new InlineTranslator("{0} instanceof soot.jimple.Constant"));
		TRAITS.put("fieldref", "local", new InlineTranslator("{0} instanceof soot.Local"));
		ATTR.put("cast_expr", "cast_type", new InlineTranslator("{0}.getCastType().toString()", "STRING"));
		ATTR.put("cast_expr", "castee", new InlineTranslator("{0}.getOp()", "value"));
		TYPE_TABLE.put("cast_expr", "soot.jimple.CastExpr");
		TRAITS.put("cast_expr", "null", new InlineTranslator("{0} instanceof soot.jimple.NullConstant"));
		TRAITS.put("cast_expr", "constant", new InlineTranslator("{0} instanceof soot.jimple.Constant"));
		TRAITS.put("cast_expr", "local", new InlineTranslator("{0} instanceof soot.Local"));
		ATTR.put("value", "kind", new BlockTranslator("soot.Value", "String", "// $BLOCK$\nif({0} instanceof soot.jimple.ArrayRef) {" +
				"\n  return \"ArrayRead\";\n} else if({0} instanceof soot.jimple.InstanceFieldRef) {\n  return \"InstanceField\";\n} " +
				"else if({0} instanceof soot.jimple.StaticFieldRef) {\n  return \"StaticField\";\n} " +
				"else if({0} instanceof soot.grimp.internal.GNewInvokeExpr) {\n  return \"New\";\n} " +
				"else if({0} instanceof soot.jimple.internal.AbstractBinopExpr) {\n  return \"Binop\";\n} " +
				"else if({0} instanceof soot.jimple.CastExpr) {\n  return \"Cast\";\n} " +
				"else if({0} instanceof soot.jimple.internal.AbstractUnopExpr) {\n  return \"Unop\";\n} " +
				"else if({0} instanceof soot.jimple.StaticInvokeExpr) {\n  return \"StaticInvoke\";\n} " +
				"else if({0} instanceof soot.jimple.InstanceInvokeExpr) {\n  return \"InstanceInvoke\";\n} " +
				"else if({0} instanceof soot.jimple.Constant) {\n   return \"Constant\";\n} " +
				"else {\n   return \"Other\";\n}\n", "STRING"));
		ATTR.put("value", "type", new InlineTranslator("{0}.getType().toString()", "STRING"));
		TYPE_TABLE.put("value", "soot.Value");
		TRAITS.put("value", "null", new InlineTranslator("{0} instanceof soot.jimple.NullConstant"));
		TRAITS.put("value", "constant", new InlineTranslator("{0} instanceof soot.jimple.Constant"));
		TRAITS.put("value", "local", new InlineTranslator("{0} instanceof soot.Local"));
		ATTR.put("assign_stmt", "lhs", new InlineTranslator("{0}.getLeftOp()", "value"));
		ATTR.put("assign_stmt", "rhs", new InlineTranslator("{0}.getRightOp()", "value"));
		TYPE_TABLE.put("assign_stmt", "soot.jimple.AssignStmt");
		ATTR.put("field", "type", new InlineTranslator("{0}.type().toString()", "STRING"));
		ATTR.put("field", "name", new InlineTranslator("{0}.name()", "STRING"));
		ATTR.put("field", "declaringClass", new InlineTranslator("{0}.declaringClass().getName()", "STRING"));
		TYPE_TABLE.put("field", "soot.SootFieldRef");
		TRAITS.put("field", "static", new InlineTranslator("{0}.isStatic()"));
		ATTR.put("invoke_stmt", "method_call", new InlineTranslator("{0}.getInvokeExpr()", "method_call"));
		TYPE_TABLE.put("invoke_stmt", "soot.jimple.InvokeStmt");
		ATTR.put("method", "declaringClass", new InlineTranslator("{0}.getDeclaringClass().getName()", "STRING"));
		ATTR.put("method", "returnType", new InlineTranslator("{0}.getReturnType().toString()", "STRING"));
		ATTR.put("method", "paramTypes", new BlockTranslator("soot.SootMethodRef", "java.util.List<String>", "// $BLOCK$\n" +
				"java.util.List<String> toReturn = new java.util.ArrayList<>();\n" +
				"for(Type t : {0}.parameterTypes()) {\n  " +
				"toReturn.add(t.toString());\n" +
				"}\n" +
				"return toReturn;\n", "STRING"));
		ATTR.put("method", "name", new InlineTranslator("{0}.name()", "STRING"));
		TYPE_TABLE.put("method", "soot.SootMethodRef");
		AVAIL_ATTR.put("unop", "operand", "unop");
		AVAIL_ATTR.put("unop", "kind", "value");
		AVAIL_ATTR.put("unop", "type", "value");
		AVAIL_TRAITS.put("unop", "null", "value");
		AVAIL_TRAITS.put("unop", "constant", "value");
		AVAIL_TRAITS.put("unop", "local", "value");
		AVAIL_ATTR.put("method_call", "args", "method_call");
		AVAIL_ATTR.put("method_call", "method", "method_call");
		AVAIL_ATTR.put("method_call", "receiver", "instance_method_call");
		AVAIL_ATTR.put("method_call", "kind", "value");
		AVAIL_ATTR.put("method_call", "type", "value");
		AVAIL_TRAITS.put("method_call", "null", "value");
		AVAIL_TRAITS.put("method_call", "constant", "value");
		AVAIL_TRAITS.put("method_call", "local", "value");
		AVAIL_ATTR.put("array_ref", "index", "array_ref");
		AVAIL_ATTR.put("array_ref", "array", "array_ref");
		AVAIL_ATTR.put("array_ref", "kind", "value");
		AVAIL_ATTR.put("array_ref", "type", "value");
		AVAIL_TRAITS.put("array_ref", "null", "value");
		AVAIL_TRAITS.put("array_ref", "constant", "value");
		AVAIL_TRAITS.put("array_ref", "local", "value");
		AVAIL_ATTR.put("binop", "kind", "value");
		AVAIL_ATTR.put("binop", "type", "value");
		AVAIL_TRAITS.put("binop", "null", "value");
		AVAIL_TRAITS.put("binop", "constant", "value");
		AVAIL_TRAITS.put("binop", "local", "value");
		AVAIL_ATTR.put("binop", "operands", "binop");
		AVAIL_ATTR.put("binop", "lop", "binop");
		AVAIL_ATTR.put("binop", "rop", "binop");
		AVAIL_ATTR.put("alloc", "allocType", "alloc");
		AVAIL_ATTR.put("alloc", "constrArgs", "alloc");
		AVAIL_ATTR.put("alloc", "kind", "value");
		AVAIL_ATTR.put("alloc", "type", "value");
		AVAIL_TRAITS.put("alloc", "null", "value");
		AVAIL_TRAITS.put("alloc", "constant", "value");
		AVAIL_TRAITS.put("alloc", "local", "value");
		AVAIL_ATTR.put("stmt", "method_call", "invoke_stmt");
		AVAIL_ATTR.put("stmt", "kind", "stmt");
		AVAIL_ATTR.put("stmt", "lhs", "assign_stmt");
		AVAIL_ATTR.put("stmt", "rhs", "assign_stmt");
		AVAIL_ATTR.put("new_array", "size", "new_array");
		AVAIL_ATTR.put("new_array", "baseType", "new_array");
		AVAIL_ATTR.put("new_array", "kind", "value");
		AVAIL_ATTR.put("new_array", "type", "value");
		AVAIL_TRAITS.put("new_array", "null", "value");
		AVAIL_TRAITS.put("new_array", "constant", "value");
		AVAIL_TRAITS.put("new_array", "local", "value");
		AVAIL_ATTR.put("instance_fieldref", "field", "fieldref");
		AVAIL_ATTR.put("instance_fieldref", "kind", "value");
		AVAIL_ATTR.put("instance_fieldref", "type", "value");
		AVAIL_TRAITS.put("instance_fieldref", "null", "value");
		AVAIL_TRAITS.put("instance_fieldref", "constant", "value");
		AVAIL_TRAITS.put("instance_fieldref", "local", "value");
		AVAIL_ATTR.put("instance_fieldref", "base_ptr", "instance_fieldref");
		AVAIL_ATTR.put("instance_method_call", "args", "method_call");
		AVAIL_ATTR.put("instance_method_call", "method", "method_call");
		AVAIL_ATTR.put("instance_method_call", "receiver", "instance_method_call");
		AVAIL_ATTR.put("instance_method_call", "kind", "value");
		AVAIL_ATTR.put("instance_method_call", "type", "value");
		AVAIL_TRAITS.put("instance_method_call", "null", "value");
		AVAIL_TRAITS.put("instance_method_call", "constant", "value");
		AVAIL_TRAITS.put("instance_method_call", "local", "value");
		AVAIL_ATTR.put("fieldref", "field", "fieldref");
		AVAIL_ATTR.put("fieldref", "kind", "value");
		AVAIL_ATTR.put("fieldref", "type", "value");
		AVAIL_TRAITS.put("fieldref", "null", "value");
		AVAIL_TRAITS.put("fieldref", "constant", "value");
		AVAIL_TRAITS.put("fieldref", "local", "value");
		AVAIL_ATTR.put("fieldref", "base_ptr", "instance_fieldref");
		AVAIL_ATTR.put("cast_expr", "cast_type", "cast_expr");
		AVAIL_ATTR.put("cast_expr", "castee", "cast_expr");
		AVAIL_ATTR.put("cast_expr", "kind", "value");
		AVAIL_ATTR.put("cast_expr", "type", "value");
		AVAIL_TRAITS.put("cast_expr", "null", "value");
		AVAIL_TRAITS.put("cast_expr", "constant", "value");
		AVAIL_TRAITS.put("cast_expr", "local", "value");
		AVAIL_ATTR.put("value", "operand", "unop");
		AVAIL_ATTR.put("value", "args", "method_call");
		AVAIL_ATTR.put("value", "method", "method_call");
		AVAIL_ATTR.put("value", "allocType", "alloc");
		AVAIL_ATTR.put("value", "constrArgs", "alloc");
		AVAIL_ATTR.put("value", "field", "fieldref");
		AVAIL_ATTR.put("value", "index", "array_ref");
		AVAIL_ATTR.put("value", "array", "array_ref");
		AVAIL_ATTR.put("value", "size", "new_array");
		AVAIL_ATTR.put("value", "baseType", "new_array");
		AVAIL_ATTR.put("value", "operands", "binop");
		AVAIL_ATTR.put("value", "lop", "binop");
		AVAIL_ATTR.put("value", "rop", "binop");
		AVAIL_ATTR.put("value", "receiver", "instance_method_call");
		AVAIL_ATTR.put("value", "kind", "value");
		AVAIL_ATTR.put("value", "type", "value");
		AVAIL_TRAITS.put("value", "null", "value");
		AVAIL_TRAITS.put("value", "constant", "value");
		AVAIL_TRAITS.put("value", "local", "value");
		AVAIL_ATTR.put("value", "cast_type", "cast_expr");
		AVAIL_ATTR.put("value", "castee", "cast_expr");
		AVAIL_ATTR.put("value", "base_ptr", "instance_fieldref");
		AVAIL_ATTR.put("assign_stmt", "kind", "stmt");
		AVAIL_ATTR.put("assign_stmt", "lhs", "assign_stmt");
		AVAIL_ATTR.put("assign_stmt", "rhs", "assign_stmt");
		AVAIL_ATTR.put("field", "type", "field");
		AVAIL_ATTR.put("field", "name", "field");
		AVAIL_ATTR.put("field", "declaringClass", "field");
		AVAIL_TRAITS.put("field", "static", "field");
		AVAIL_ATTR.put("invoke_stmt", "method_call", "invoke_stmt");
		AVAIL_ATTR.put("invoke_stmt", "kind", "stmt");
		AVAIL_ATTR.put("method", "declaringClass", "method");
		AVAIL_ATTR.put("method", "returnType", "method");
		AVAIL_ATTR.put("method", "paramTypes", "method");
		AVAIL_ATTR.put("method", "name", "method");
	}
	
	public static class CompileContext {
		public HashMap<String, String> utilMethods = new HashMap<>();
		public void addUtilityMethod(final String name, final String body) {
			utilMethods.put(name, body);
		}
		
		public void dumpHelperMethods(final StringBuilder ps) {
			for(final String m : utilMethods.values()) {
				ps.append(m).append('\n');
			}
		}
	}
	
	public String translateTrait(final String inputValue, final String valueType, final List<String> attrList, final String trait, final boolean is, final CompileContext ctxt) {
		return this.translateLoop(attrList, ctxt, inputValue, valueType, new F2<String, String, String>() {
			@Override
			public String f(final String a, final String b) {
				if(!AVAIL_TRAITS.contains(b, trait)) {
					throw new IllegalArgumentException();
				}
				final String declaringType = AVAIL_TRAITS.get(b, trait);
				final String javaType = TYPE_TABLE.get(declaringType);
				final String casted = String.format("((%s)%s)", javaType, a);
				final Translator t = TRAITS.get(declaringType, trait);
				final String translated = t.translate(casted, declaringType, trait, ctxt);
				if(!is) {
					return "!(" + translated + ")";
				}
				return translated;
			}
		});
	}

	public String translateComparison(final String inputValue, final String valueType,
			final List<String> attributeList, final String targetConstant, final String op, final CompileContext ctxt) {
		final String accum = inputValue;
		final String currType = valueType;
		final F2<String, String, String> cont = new F2<String, String, String>() {
			@Override
			public String f(final String accum, final String currType) {
				if(currType.equals("STRING")) {
					final String equalsCheck = accum + ".equals(\"" + targetConstant + "\")";
					if(op.equals("=")) {
						return equalsCheck;
					} else {
						return "!" + equalsCheck;
					}
				} else {
					return accum + " " + op + " " + targetConstant; 
				}
			}
		};
		return translateLoop(attributeList, ctxt, accum, currType, cont);
	}

	private String translateLoop(final List<String> attributeList, final CompileContext ctxt, String accum, String currType,
			final F2<String, String, String> cont) {
		for(int i = 0; i < attributeList.size(); i++) {
			final String currAttr = attributeList.get(i);
			if(!AVAIL_ATTR.contains(currType, currAttr)) {
				throw new IllegalArgumentException(currType + " " + currAttr + " " + accum);
			}
			final String containingType = AVAIL_ATTR.get(currType, currAttr);
			final Translator t = ATTR.get(containingType, currAttr);
			final String targetType = TYPE_TABLE.get(containingType);
			final String casted = String.format("((%s)%s)", targetType, accum);
			
			if(i < attributeList.size() - 1 && isIndex(attributeList.get(i+1))) {
				final String elemType = t.getOutputType();
				final String elemJavaType = TYPE_TABLE.get(elemType);
				final String toIterate = t.translate(casted, containingType, currAttr, ctxt);
				
				final String perElemPred = this.translateLoop(attributeList.subList(i+2, attributeList.size()), ctxt, "arg", elemType, cont);
				return String.format("fj.data.Stream.iterableStream(%s).forall(new fj.F<%s, Boolean>() { public Boolean f(%s arg) { return %s; } })",
					toIterate, elemJavaType, elemJavaType, perElemPred
				);
			} else if(i == attributeList.size() - 2 && attributeList.get(i+1).equals("length")) {
				accum = t.translate(casted, containingType, currAttr, ctxt) + ".size()";
				currType = "INT";
				break;
			} else {
				final String newType = t.getOutputType();
				accum = t.translate(casted, containingType, currAttr, ctxt);
				currType = newType;
			}
		}
		return cont.f(accum, currType);
	}

	private boolean isIndex(final String string) {
		return string.equals("*");
	}
	
	public static void main(final String[] args) {
    final StringBuilder sb = new StringBuilder(64);
    sb.append("package codestats;\n");
    sb.append("public class QueryInterpreter$Impl implements edu.washington.cse.codestats.QueryInterpreter {\n");
    final CompileContext context = new CompileContext();
    final Compiler c = new Compiler();
    final List<Query> prog = new ArrayList<>();
    prog.add(new SumMirror() {
			
			@Override
			public QueryTarget target() {
				return QueryTarget.EXPRESSION;
			}
			
			@Override
			public String name() {
				return "hello";
			}
			
			@Override
			public PredicateMirror getPredicate() {
				return new PredicateMirror() {
					@Override
					public PredicateType getType() {
						return PredicateType.AND;
					}

					@Override
					public List<PredicateMirror> childPredicates() {
						final List<PredicateMirror> toReturn = new ArrayList<PredicateMirror>();
						toReturn.add(new PredicateMirror() {
							@Override
							public PredicateType getType() {
								return PredicateType.ATOM;
							}

							@Override
							public List<PredicateMirror> childPredicates() {
								return null;
							}

							@Override
							public PredicateAtom atom() {
								return new PredicateAtom() {
									@Override
									public Type type() {
										return Type.ATTRIBUTE_CHECK;
									}
									
									@Override
									public String target() {
										return "Invoke";
									}
									
									@Override
									public String op() {
										return "=";
									}
									
									@Override
									public boolean is() {
										return false;
									}
									
									@Override
									public List<String> attributeList() {
										return Collections.singletonList("kind");
									}
								};
							}
						});
						toReturn.add(new PredicateMirror() {
							@Override
							public PredicateType getType() {
								return PredicateType.ATOM;
							}

							@Override
							public List<PredicateMirror> childPredicates() {
								return null;
							}

							@Override
							public PredicateAtom atom() {
								return new PredicateAtom() {
									@Override
									public boolean is() {
										return true;
									}

									@Override
									public String target() {
										return "constant";
									}

									@Override
									public String op() {
										return null;
									}

									@Override
									public Type type() {
										return Type.TRAIT_CHECK;
									}

									@Override
									public List<String> attributeList() {
										return Arrays.asList("args", "*");
									}
								};
							}
						});
						return toReturn;
					}

					@Override
					public PredicateAtom atom() {
						return null;
					}
					
				};
			}
			
			@Override
			public String deriving() {
				return null;
			}
		});
    
    final List<Query> stmtQueries = new ArrayList<>(); 
    final List<Query> exprQueries = new ArrayList<>();
    for(final Query q : prog) {
    	if(q.target() == QueryTarget.STATEMENT) {
    		stmtQueries.add(q);
    	} else {
    		exprQueries.add(q);
    	}
    }
    addInterpreter(sb, context, c, exprQueries, "soot.Value", "value");
    addInterpreter(sb, context, c, stmtQueries, "soot.jimple.Stmt", "stmt");
    context.dumpHelperMethods(sb); 
    sb.append("}\n");

    final File helloWorldJava = new File("codestats/QueryInterpreter$Impl.java");
    if (helloWorldJava.getParentFile().exists() || helloWorldJava.getParentFile().mkdirs()) {

        try {
            Writer writer = null;
            try {
                writer = new FileWriter(helloWorldJava);
                writer.write(sb.toString());
                writer.flush();
            } finally {
                try {
                    writer.close();
                } catch (final Exception e) {
                }
            }

            /** Compilation Requirements *********************************************************************************************/
            final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
            final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            final StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

            // This sets up the class path that the compiler will use.
            // I've added the .jar file that contains the DoStuff interface within in it...
            final List<String> optionList = new ArrayList<String>();
            optionList.add("-classpath");
            optionList.add(System.getProperty("java.class.path"));

            final Iterable<? extends JavaFileObject> compilationUnit
                    = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(helloWorldJava));
            final JavaCompiler.CompilationTask task = compiler.getTask(
                null, 
                fileManager, 
                diagnostics, 
                optionList, 
                null, 
                compilationUnit);
            /********************************************************************************************* Compilation Requirements **/
            if (task.call()) {
                /************************************************************************************************* Load and execute **/
            } else {
                for (final Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                	System.out.println(diagnostic.getMessage(null));
                    System.out.format("Error on line %d in %s%n",
                            diagnostic.getLineNumber(),
                            diagnostic.getSource().toUri());
                }
            }
            fileManager.close();
        } catch (final IOException exp) {
            exp.printStackTrace();
        }
    }
	}

	private static void addInterpreter(final StringBuilder sb, final CompileContext context, final Compiler c, final List<Query> exprQueries,
			final String javaType, final String startType) {
		sb.append("public boolean interpret(String q, ").append(javaType).append(" v) {");
    for(final Query q : exprQueries) {
    	sb.append("if(q.equals(\"").append(q.name()).append("\")) {\n");
    	sb.append("return ").append(c.translatePredicate("v", startType, q.getPredicate(), context)).append(";");
    	sb.append("\n} else ");
    }
    sb.append("{ throw new java.lang.IllegalArgumentException(); }\n");
    sb.append("}");
	}

	private String translatePredicate(final String valueExpr, final String valueType, final PredicateMirror predicate, final CompileContext ctxt) {
		if(predicate.getType() == PredicateType.AND) {
			final StringBuilder sb = new StringBuilder();
			sb.append("(");
			for(final PredicateMirror pm : predicate.childPredicates()) {
				sb.append("(").append(this.translatePredicate(valueExpr, valueType, pm, ctxt)).append(") && ");
			}
			sb.append("true)");
			return sb.toString();
		} else if(predicate.getType() == PredicateType.OR) {
			final StringBuilder sb = new StringBuilder();
			sb.append("(");
			for(final PredicateMirror pm : predicate.childPredicates()) {
				sb.append("(").append(this.translatePredicate(valueExpr, valueType, pm, ctxt)).append(") || ");
			}
			sb.append("false)");
			return sb.toString();
		} else {
			final PredicateAtom atom = predicate.atom();
			if(atom.type() == Type.ATTRIBUTE_CHECK) {
				return this.translateComparison(valueExpr, valueType, atom.attributeList(), atom.target(), atom.op(), ctxt);
			} else {
				return this.translateTrait(valueExpr, valueType, atom.attributeList(), atom.target(), atom.is(), ctxt);
			}
		}
	}
}