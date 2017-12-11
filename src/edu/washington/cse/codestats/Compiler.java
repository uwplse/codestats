package edu.washington.cse.codestats;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import edu.washington.cse.codestats.PredicateAtom.Type;
import edu.washington.cse.codestats.PredicateMirror.PredicateType;
import fj.F2;

public class Compiler {
	private static final String ARG_NAME = "toCheck";
	private static final String CONTEXT_NAME = "ctxt";
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
		ATTR.put("method_call", "args", new InlineTranslator("{0}.getArgs()", "value"));
		ATTR.put("method_call", "method", new InlineTranslator("{0}.getMethodRef()", "method"));
		TYPE_TABLE.put("method_call", "soot.jimple.InvokeExpr");
		ATTR.put("array_ref", "index", new InlineTranslator("{0}.getIndex()", "value"));
		ATTR.put("array_ref", "array", new InlineTranslator("{0}.getBase()", "value"));
		TYPE_TABLE.put("array_ref", "soot.jimple.ArrayRef");
		ATTR.put("binop", "operands", new BlockTranslator("soot.jimple.internal.AbstractBinopExpr", "java.util.List<soot.Value>", "// $BLOCK$\n" + 
		"java.util.List<Value> toReturn = new java.util.ArrayList<>();\n" + 
		"toReturn.add({0}.getOp1());\n" + 
		"toReturn.add({1}.getOp2());\n" + 
		"return toReturn;\n" + 
		"\n", "value"));
		ATTR.put("binop", "lop", new InlineTranslator("{0}.getOp1()", "value"));
		ATTR.put("binop", "rop", new InlineTranslator("{0}.getOp2()", "value"));
		TYPE_TABLE.put("binop", "soot.jimple.internal.AbstractBinopExpr");
		ATTR.put("alloc", "allocType", new InlineTranslator("{0}.getBaseType().getClassName()", "STRING"));
		ATTR.put("alloc", "constrArgs", new InlineTranslator("{0}.getArgs()", "value"));
		TYPE_TABLE.put("alloc", "soot.grimp.internal.GNewInvokeExpr");
		ATTR.put("stmt", "kind", new BlockTranslator("soot.jimple.Stmt", "String", "// $BLOCK$\n" + 
		"if({0} instanceof soot.jimple.AssignStmt) {\n" + 
		"   return \"Assign\";\n" + 
		"} else if({0} instanceof soot.jimple.InvokeStmt) {\n" + 
		"   return \"Invoke\";\n" + 
		"} else if({0} instanceof soot.jimple.ReturnStmt) {\n" + 
		"   return \"Return\";\n" + 
		"} else {\n" + 
		"   return \"Other\";\n" + 
		"}\n" + 
		"\n", "STRING"));
		ATTR.put("stmt", "host", new InlineTranslator("{1}.getContainingMethod()", "method"));
		TYPE_TABLE.put("stmt", "soot.jimple.Stmt");
		ATTR.put("return_stmt", "ret_val", new InlineTranslator("{0}.getOp()", "value"));
		TYPE_TABLE.put("return_stmt", "soot.jimple.ReturnStmt");
		ATTR.put("instance_fieldref", "base_ptr", new InlineTranslator("{0}.getBase()", "value"));
		TYPE_TABLE.put("instance_fieldref", "soot.jimple.InstanceFieldRef");
		ATTR.put("instance_method_call", "receiver", new InlineTranslator("{0}.getBase()", "value"));
		TYPE_TABLE.put("instance_method_call", "soot.jimple.InstanceInvokeExpr");
		ATTR.put("fieldref", "field", new InlineTranslator("{0}.getFieldRef()", "field"));
		TYPE_TABLE.put("fieldref", "soot.jimple.FieldRef");
		ATTR.put("cast_expr", "cast_type", new InlineTranslator("{0}.getCastType().toString()", "STRING"));
		ATTR.put("cast_expr", "castee", new InlineTranslator("{0}.getOp()", "value"));
		TYPE_TABLE.put("cast_expr", "soot.jimple.CastExpr");
		ATTR.put("value", "kind", new BlockTranslator("soot.Value", "String", "// $BLOCK$\n" + 
		"if({0} instanceof soot.jimple.ArrayRef) {\n" + 
		"  return \"ArrayRead\";\n" + 
		"} else if({0} instanceof soot.jimple.InstanceFieldRef) {\n" + 
		"  return \"InstanceField\";\n" + 
		"} else if({0} instanceof soot.jimple.StaticFieldRef) {\n" + 
		"  return \"StaticField\";\n" + 
		"} else if({0} instanceof soot.grimp.internal.GNewInvokeExpr) {\n" + 
		"  return \"New\";\n" + 
		"} else if({0} instanceof soot.jimple.internal.AbstractBinopExpr) {\n" + 
		"  return \"Binop\";\n" + 
		"} else if({0} instanceof soot.jimple.CastExpr) {\n" + 
		"  return \"Cast\";\n" + 
		"} else if({0} instanceof soot.jimple.internal.AbstractUnopExpr) {\n" + 
		"  return \"Unop\";\n" + 
		"} else if({0} instanceof soot.jimple.StaticInvokeExpr) {\n" + 
		"  return \"StaticInvoke\";\n" + 
		"} else if({0} instanceof soot.jimple.InstanceInvokeExpr) {\n" + 
		"  return \"InstanceInvoke\";\n" + 
		"} else if({0} instanceof soot.jimple.Constant) {\n" + 
		"   return \"Constant\";\n" + 
		"} else if({0} instanceof soot.jimple.NewArrayExpr) {\n" + 
		"   return \"NewArray\";\n" + 
		"} else {\n" + 
		"   return \"Other\";\n" + 
		"}\n" + 
		"\n", "STRING"));
		ATTR.put("value", "type", new InlineTranslator("{0}.getType().toString()", "STRING"));
		ATTR.put("value", "host", new InlineTranslator("{1}.getContainingMethod()", "method"));
		TYPE_TABLE.put("value", "soot.Value");
		TRAITS.put("value", "null", new InlineTranslator("{0} instanceof soot.jimple.NullConstant"));
		TRAITS.put("value", "constant", new InlineTranslator("{0} instanceof soot.jimple.Constant"));
		TRAITS.put("value", "local", new InlineTranslator("{0} instanceof soot.Local"));
		TRAITS.put("value", "this", new InlineTranslator("{0} == {1}.getThisLocal()"));
		TRAITS.put("value", "arg", new InlineTranslator("{1}.getArgLocals().contains({0})"));
		ATTR.put("assign_stmt", "lhs", new InlineTranslator("{0}.getLeftOp()", "value"));
		ATTR.put("assign_stmt", "rhs", new InlineTranslator("{0}.getRightOp()", "value"));
		TYPE_TABLE.put("assign_stmt", "soot.jimple.AssignStmt");
		ATTR.put("new_array", "size", new InlineTranslator("{0}.getSize()", "value"));
		ATTR.put("new_array", "baseType", new InlineTranslator("{0}.getBaseType().toString()", "STRING"));
		TYPE_TABLE.put("new_array", "soot.jimple.NewArrayExpr");
		ATTR.put("field", "type", new InlineTranslator("{0}.type().toString()", "STRING"));
		ATTR.put("field", "name", new InlineTranslator("{0}.name()", "STRING"));
		ATTR.put("field", "declaringClass", new InlineTranslator("{0}.declaringClass().getName()", "STRING"));
		TYPE_TABLE.put("field", "soot.SootFieldRef");
		TRAITS.put("field", "static", new InlineTranslator("{0}.isStatic()"));
		ATTR.put("invoke_stmt", "method_call", new InlineTranslator("{0}.getInvokeExpr()", "method_call"));
		TYPE_TABLE.put("invoke_stmt", "soot.jimple.InvokeStmt");
		ATTR.put("method", "declaringClass", new InlineTranslator("{0}.declaringClass().getName()", "STRING"));
		ATTR.put("method", "returnType", new InlineTranslator("{0}.getReturnType().toString()", "STRING"));
		ATTR.put("method", "paramTypes", new BlockTranslator("soot.SootMethodRef", "java.util.List<String>", "// $BLOCK$\n" + 
		"java.util.List<String> toReturn = new java.util.ArrayList<>();\n" + 
		"for(Type t : {0}.parameterTypes()) {\n" + 
		"  toReturn.add(t.toString());\n" + 
		"}\n" + 
		"return toReturn;\n" + 
		"\n", "STRING"));
		ATTR.put("method", "name", new InlineTranslator("{0}.name()", "STRING"));
		ATTR.put("method", "signature", new InlineTranslator("{0}.getSignature()", "STRING"));
		TYPE_TABLE.put("method", "soot.SootMethodRef");
		TRAITS.put("method", "static", new InlineTranslator("{0}.isStatic()"));
		AVAIL_ATTR.put("unop", "operand", "unop");
		AVAIL_ATTR.put("unop", "kind", "value");
		AVAIL_ATTR.put("unop", "type", "value");
		AVAIL_ATTR.put("unop", "host", "value");
		AVAIL_TRAITS.put("unop", "null", "value");
		AVAIL_TRAITS.put("unop", "constant", "value");
		AVAIL_TRAITS.put("unop", "local", "value");
		AVAIL_TRAITS.put("unop", "this", "value");
		AVAIL_TRAITS.put("unop", "arg", "value");
		AVAIL_ATTR.put("method_call", "args", "method_call");
		AVAIL_ATTR.put("method_call", "method", "method_call");
		AVAIL_ATTR.put("method_call", "receiver", "instance_method_call");
		AVAIL_ATTR.put("method_call", "kind", "value");
		AVAIL_ATTR.put("method_call", "type", "value");
		AVAIL_ATTR.put("method_call", "host", "value");
		AVAIL_TRAITS.put("method_call", "null", "value");
		AVAIL_TRAITS.put("method_call", "constant", "value");
		AVAIL_TRAITS.put("method_call", "local", "value");
		AVAIL_TRAITS.put("method_call", "this", "value");
		AVAIL_TRAITS.put("method_call", "arg", "value");
		AVAIL_ATTR.put("array_ref", "index", "array_ref");
		AVAIL_ATTR.put("array_ref", "array", "array_ref");
		AVAIL_ATTR.put("array_ref", "kind", "value");
		AVAIL_ATTR.put("array_ref", "type", "value");
		AVAIL_ATTR.put("array_ref", "host", "value");
		AVAIL_TRAITS.put("array_ref", "null", "value");
		AVAIL_TRAITS.put("array_ref", "constant", "value");
		AVAIL_TRAITS.put("array_ref", "local", "value");
		AVAIL_TRAITS.put("array_ref", "this", "value");
		AVAIL_TRAITS.put("array_ref", "arg", "value");
		AVAIL_ATTR.put("binop", "kind", "value");
		AVAIL_ATTR.put("binop", "type", "value");
		AVAIL_ATTR.put("binop", "host", "value");
		AVAIL_TRAITS.put("binop", "null", "value");
		AVAIL_TRAITS.put("binop", "constant", "value");
		AVAIL_TRAITS.put("binop", "local", "value");
		AVAIL_TRAITS.put("binop", "this", "value");
		AVAIL_TRAITS.put("binop", "arg", "value");
		AVAIL_ATTR.put("binop", "operands", "binop");
		AVAIL_ATTR.put("binop", "lop", "binop");
		AVAIL_ATTR.put("binop", "rop", "binop");
		AVAIL_ATTR.put("alloc", "allocType", "alloc");
		AVAIL_ATTR.put("alloc", "constrArgs", "alloc");
		AVAIL_ATTR.put("alloc", "kind", "value");
		AVAIL_ATTR.put("alloc", "type", "value");
		AVAIL_ATTR.put("alloc", "host", "value");
		AVAIL_TRAITS.put("alloc", "null", "value");
		AVAIL_TRAITS.put("alloc", "constant", "value");
		AVAIL_TRAITS.put("alloc", "local", "value");
		AVAIL_TRAITS.put("alloc", "this", "value");
		AVAIL_TRAITS.put("alloc", "arg", "value");
		AVAIL_ATTR.put("stmt", "method_call", "invoke_stmt");
		AVAIL_ATTR.put("stmt", "ret_val", "return_stmt");
		AVAIL_ATTR.put("stmt", "kind", "stmt");
		AVAIL_ATTR.put("stmt", "host", "stmt");
		AVAIL_ATTR.put("stmt", "lhs", "assign_stmt");
		AVAIL_ATTR.put("stmt", "rhs", "assign_stmt");
		AVAIL_ATTR.put("return_stmt", "ret_val", "return_stmt");
		AVAIL_ATTR.put("return_stmt", "kind", "stmt");
		AVAIL_ATTR.put("return_stmt", "host", "stmt");
		AVAIL_ATTR.put("instance_fieldref", "field", "fieldref");
		AVAIL_ATTR.put("instance_fieldref", "kind", "value");
		AVAIL_ATTR.put("instance_fieldref", "type", "value");
		AVAIL_ATTR.put("instance_fieldref", "host", "value");
		AVAIL_TRAITS.put("instance_fieldref", "null", "value");
		AVAIL_TRAITS.put("instance_fieldref", "constant", "value");
		AVAIL_TRAITS.put("instance_fieldref", "local", "value");
		AVAIL_TRAITS.put("instance_fieldref", "this", "value");
		AVAIL_TRAITS.put("instance_fieldref", "arg", "value");
		AVAIL_ATTR.put("instance_fieldref", "base_ptr", "instance_fieldref");
		AVAIL_ATTR.put("instance_method_call", "args", "method_call");
		AVAIL_ATTR.put("instance_method_call", "method", "method_call");
		AVAIL_ATTR.put("instance_method_call", "receiver", "instance_method_call");
		AVAIL_ATTR.put("instance_method_call", "kind", "value");
		AVAIL_ATTR.put("instance_method_call", "type", "value");
		AVAIL_ATTR.put("instance_method_call", "host", "value");
		AVAIL_TRAITS.put("instance_method_call", "null", "value");
		AVAIL_TRAITS.put("instance_method_call", "constant", "value");
		AVAIL_TRAITS.put("instance_method_call", "local", "value");
		AVAIL_TRAITS.put("instance_method_call", "this", "value");
		AVAIL_TRAITS.put("instance_method_call", "arg", "value");
		AVAIL_ATTR.put("fieldref", "field", "fieldref");
		AVAIL_ATTR.put("fieldref", "kind", "value");
		AVAIL_ATTR.put("fieldref", "type", "value");
		AVAIL_ATTR.put("fieldref", "host", "value");
		AVAIL_TRAITS.put("fieldref", "null", "value");
		AVAIL_TRAITS.put("fieldref", "constant", "value");
		AVAIL_TRAITS.put("fieldref", "local", "value");
		AVAIL_TRAITS.put("fieldref", "this", "value");
		AVAIL_TRAITS.put("fieldref", "arg", "value");
		AVAIL_ATTR.put("fieldref", "base_ptr", "instance_fieldref");
		AVAIL_ATTR.put("cast_expr", "cast_type", "cast_expr");
		AVAIL_ATTR.put("cast_expr", "castee", "cast_expr");
		AVAIL_ATTR.put("cast_expr", "kind", "value");
		AVAIL_ATTR.put("cast_expr", "type", "value");
		AVAIL_ATTR.put("cast_expr", "host", "value");
		AVAIL_TRAITS.put("cast_expr", "null", "value");
		AVAIL_TRAITS.put("cast_expr", "constant", "value");
		AVAIL_TRAITS.put("cast_expr", "local", "value");
		AVAIL_TRAITS.put("cast_expr", "this", "value");
		AVAIL_TRAITS.put("cast_expr", "arg", "value");
		AVAIL_ATTR.put("value", "operand", "unop");
		AVAIL_ATTR.put("value", "args", "method_call");
		AVAIL_ATTR.put("value", "method", "method_call");
		AVAIL_ATTR.put("value", "allocType", "alloc");
		AVAIL_ATTR.put("value", "constrArgs", "alloc");
		AVAIL_ATTR.put("value", "field", "fieldref");
		AVAIL_ATTR.put("value", "index", "array_ref");
		AVAIL_ATTR.put("value", "array", "array_ref");
		AVAIL_ATTR.put("value", "operands", "binop");
		AVAIL_ATTR.put("value", "lop", "binop");
		AVAIL_ATTR.put("value", "rop", "binop");
		AVAIL_ATTR.put("value", "receiver", "instance_method_call");
		AVAIL_ATTR.put("value", "kind", "value");
		AVAIL_ATTR.put("value", "type", "value");
		AVAIL_ATTR.put("value", "host", "value");
		AVAIL_TRAITS.put("value", "null", "value");
		AVAIL_TRAITS.put("value", "constant", "value");
		AVAIL_TRAITS.put("value", "local", "value");
		AVAIL_TRAITS.put("value", "this", "value");
		AVAIL_TRAITS.put("value", "arg", "value");
		AVAIL_ATTR.put("value", "size", "new_array");
		AVAIL_ATTR.put("value", "baseType", "new_array");
		AVAIL_ATTR.put("value", "cast_type", "cast_expr");
		AVAIL_ATTR.put("value", "castee", "cast_expr");
		AVAIL_ATTR.put("value", "base_ptr", "instance_fieldref");
		AVAIL_ATTR.put("assign_stmt", "kind", "stmt");
		AVAIL_ATTR.put("assign_stmt", "host", "stmt");
		AVAIL_ATTR.put("assign_stmt", "lhs", "assign_stmt");
		AVAIL_ATTR.put("assign_stmt", "rhs", "assign_stmt");
		AVAIL_ATTR.put("new_array", "size", "new_array");
		AVAIL_ATTR.put("new_array", "baseType", "new_array");
		AVAIL_ATTR.put("new_array", "kind", "value");
		AVAIL_ATTR.put("new_array", "type", "value");
		AVAIL_ATTR.put("new_array", "host", "value");
		AVAIL_TRAITS.put("new_array", "null", "value");
		AVAIL_TRAITS.put("new_array", "constant", "value");
		AVAIL_TRAITS.put("new_array", "local", "value");
		AVAIL_TRAITS.put("new_array", "this", "value");
		AVAIL_TRAITS.put("new_array", "arg", "value");
		AVAIL_ATTR.put("field", "type", "field");
		AVAIL_ATTR.put("field", "name", "field");
		AVAIL_ATTR.put("field", "declaringClass", "field");
		AVAIL_TRAITS.put("field", "static", "field");
		AVAIL_ATTR.put("invoke_stmt", "method_call", "invoke_stmt");
		AVAIL_ATTR.put("invoke_stmt", "kind", "stmt");
		AVAIL_ATTR.put("invoke_stmt", "host", "stmt");
		AVAIL_ATTR.put("method", "declaringClass", "method");
		AVAIL_ATTR.put("method", "returnType", "method");
		AVAIL_ATTR.put("method", "paramTypes", "method");
		AVAIL_ATTR.put("method", "name", "method");
		AVAIL_ATTR.put("method", "signature", "method");
		AVAIL_TRAITS.put("method", "static", "method");
	}
	
	public static class CompileContext {
		public HashMap<String, String> utilMethods = new HashMap<>();
		public HashMap<String, List<String>> stringSets = new HashMap<>();
		private final HashMap<String, List<String>> intSets = new HashMap<>();
		public void addUtilityMethod(final String name, final String body) {
			utilMethods.put(name, body);
		}
		
		public void dumpHelperMethods(final StringBuilder ps) {
			for(final String m : utilMethods.values()) {
				ps.append(m).append('\n');
			}
			
			for(final String intSetName : intSets.keySet()) {
				ps.append("private java.util.Set<java.lang.Integer> ").append(intSetName).append(" = new java.util.HashSet<>();\n");
			}
			for(final String intSetName : stringSets.keySet()) {
				ps.append("private java.util.Set<java.lang.String> ").append(intSetName).append(" = new java.util.HashSet<>();\n");
			}
			ps.append("{\n");
			for(final Map.Entry<String, List<String>> strSetSpec : stringSets.entrySet()) {
				final String varName = strSetSpec.getKey();
				for(final String elem : strSetSpec.getValue()) {
					ps.append(varName).append(".add(\"").append(elem.substring(1, elem.length() - 1)).append("\");\n");
				}
			}
			for(final Map.Entry<String, List<String>> intSetSpec : intSets.entrySet()) {
				final String varName = intSetSpec.getKey();
				for(final String elem : intSetSpec.getValue()) {
					ps.append(varName).append(".add(").append(elem).append(");\n");
				}
			}
			ps.append("}\n");
		}

		public void addStringSet(final String fieldName, final List<String> members) {
			this.stringSets.put(fieldName, members);
		}

		public void addIntSet(final String fieldName, final List<String> members) {
			this.intSets .put(fieldName, members);
		}
	}
	
	public String translateTrait(final String inputValue, final String valueType, final List<String> attrList, final String trait, final boolean is, final CompileContext ctxt) {
		return this.translateLoop(attrList, ctxt, inputValue, valueType, new F2<String, String, String>() {
			@Override
			public String f(final String a, final String b) {
				if(!AVAIL_TRAITS.contains(b, trait)) {
					throw new IllegalArgumentException(a + ", " + b + ", " + trait);
				}
				final String declaringType = AVAIL_TRAITS.get(b, trait);
				final String javaType = TYPE_TABLE.get(declaringType);
				final String casted = String.format("((%s)%s)", javaType, a);
				final Translator t = TRAITS.get(declaringType, trait);
				final String translated = t.translate(casted, declaringType, trait, ctxt, CONTEXT_NAME);
				if(!is) {
					return "!(" + translated + ")";
				}
				return translated;
			}
		});
	}
	
	private static int inCounter = 0;

	public String translateComparison(final String inputValue, final String valueType,
			final List<String> attributeList, final PredicateAtom atom, final String op, final CompileContext ctxt) {
		final String accum = inputValue;
		final String currType = valueType;
		final F2<String, String, String> cont = new F2<String, String, String>() {
			@Override
			public String f(final String accum, final String currType) {
				String target;
				if(op.equals("in")) {
					final String setName;
					if(currType.equals("STRING")) {
						setName = "in_string_set_" + (inCounter++);
						ctxt.addStringSet(setName, atom.targetList());
					} else {
						setName = "in_int_set_" + (inCounter++);
						ctxt.addIntSet(setName, atom.targetList());
					}
					return setName + ".contains(" + accum + ")";
				}
				if(atom.targetList() == null) {
					target = "\"" + atom.target().substring(1, atom.target().length() - 1) + "\"";
				} else {
					target = translateLoop(atom.targetList(), ctxt, inputValue, valueType, new F2<String, String, String>() {
						@Override
						public String f(final String accum, final String typ) {
							if(!typ.equals(currType)) {
								throw new RuntimeException("Type mismatch!"); 
							}
							return accum;
						}
					});
				}
				if(currType.equals("STRING")) {
					final String equalsCheck = accum + ".equals(" + target + ")";
					if(op.equals("==")) {
						return equalsCheck;
					} else {
						return "!" + equalsCheck;
					}
				} else {
					return accum + " " + op + " " + target; 
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
				final String toIterate = t.translate(casted, containingType, currAttr, ctxt, CONTEXT_NAME);
				final String index = attributeList.get(i+1);
				if(index.equals("*")) {
					this.addForallExists(ctxt);
					final String perElemPred = this.translateLoop(attributeList.subList(i+2, attributeList.size()), ctxt, "arg", elemType, cont);
					return String.format("this.forallExists(%s, new fj.F<%s, Boolean>() { public Boolean f(%s arg) { return %s; } })",
						toIterate, elemJavaType, elemJavaType, perElemPred
					);
				} else if(index.equals("?")) {
					this.addAnyExists(ctxt);
					final String perElementPred = this.translateLoop(attributeList.subList(i+2, attributeList.size()), ctxt, "arg", elemType, cont);
					return String.format("this.anyExists(%s, new fj.F<%s, Boolean>() { public Boolean f(%s arg) { return %s; } })",
						toIterate, elemJavaType, elemJavaType, perElementPred
					);
				} else {
					accum = toIterate + ".get(" + attributeList.get(i + 1) + ")";
					currType = t.getOutputType();
					i++;
				}
			} else if(i == attributeList.size() - 2 && attributeList.get(i+1).equals("length")) {
				accum = t.translate(casted, containingType, currAttr, ctxt, CONTEXT_NAME) + ".size()";
				currType = "INT";
				break;
			} else {
				final String newType = t.getOutputType();
				accum = t.translate(casted, containingType, currAttr, ctxt, CONTEXT_NAME);
				currType = newType;
			}
		}
		return cont.f(accum, currType);
	}

	private void addAnyExists(final CompileContext ctxt) {
		ctxt.addUtilityMethod("forallExists", "public <T> boolean anyExists(java.lang.Iterable<T> toStream, fj.F<T, Boolean> pred) {\n" +
				"fj.data.Stream<T> stream = fj.data.Stream.iterableStream(toStream);\n" +
				"return stream.isNotEmpty() && stream.exists(pred);\n" +
				"}\n");
	}

	private void addForallExists(final CompileContext ctxt) {
		ctxt.addUtilityMethod("forallExists", "public <T> boolean forallExists(java.lang.Iterable<T> toStream, fj.F<T, Boolean> pred) {\n" +
				"fj.data.Stream<T> stream = fj.data.Stream.iterableStream(toStream);\n" +
				"return stream.isNotEmpty() && stream.forall(pred);\n" +
				"}\n");
	}

	private boolean isIndex(final String string) {
		return string.equals("*") || string.equals("?") || NumberUtils.isNumber(string);
	}
	
	private static File compileProgram(final List<Query> prog) {
		final StringBuilder sb = new StringBuilder(64);
    sb.append("package codestats;\n");
    sb.append("public class QueryInterpreterImpl implements edu.washington.cse.codestats.QueryInterpreter {\n");
    final CompileContext context = new CompileContext();
    final Compiler c = new Compiler();
    
    final List<Query> stmtQueries = new ArrayList<>(); 
    final List<Query> exprQueries = new ArrayList<>();
    for(final Query q : prog) {
    	if(q.target() == QueryTarget.STATEMENT) {
    		stmtQueries.add(q);
    	} else {
    		exprQueries.add(q);
    	}
    }
    addInterpreter(sb, context, c, exprQueries, "soot.ValueBox", "value", ".getValue()");
    addInterpreter(sb, context, c, stmtQueries, "soot.jimple.Stmt", "stmt", "");
    context.dumpHelperMethods(sb); 
    sb.append("}\n");

    final File queryImplSource = new File("tmp/codestats/QueryInterpreterImpl.java");
		if(queryImplSource.getParentFile().exists() || queryImplSource.getParentFile().mkdirs()) {
			try {
				Writer writer = null;
				try {
					writer = new FileWriter(queryImplSource);
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
				// I've added the .jar file that contains the DoStuff interface within
				// in it...
				final List<String> optionList = new ArrayList<String>();
				optionList.add("-classpath");
				optionList.add(System.getProperty("java.class.path"));

				final Iterable<? extends JavaFileObject> compilationUnit = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(queryImplSource));
				final JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionList, null, compilationUnit);
				/********************************************************************************************* Compilation Requirements **/
				if(task.call()) {
					/************************************************************************************************* Load and execute **/
				} else {
					for(final Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
						System.out.println(diagnostic.getMessage(null));
						System.out.format("Error on line %d in %s%n", diagnostic.getLineNumber(), diagnostic.getSource().toUri());
					}
					throw new RuntimeException();
				}
				fileManager.close();
			} catch (final IOException exp) {
				exp.printStackTrace();
			}
		}
		return new File("tmp/codestats/QueryInterpreterImpl.class");
	}

	private static void addInterpreter(final StringBuilder sb, final CompileContext context, final Compiler c, final List<Query> exprQueries,
			final String javaType, final String startType, final String startTransformer) {
		sb.append("public boolean interpret(String q, final ").append(javaType).append(" ").append(ARG_NAME).append(", final ").append(QueryContext.class.getName()).append(" ").append(CONTEXT_NAME).append(") {\n");
    for(final Query q : exprQueries) {
    	sb.append("if(q.equals(\"").append(q.name()).append("\")) {\n");
    	sb.append("return ").append(c.translatePredicate(ARG_NAME + startTransformer, startType, q.getPredicate(), context)).append(";");
    	sb.append("\n} else ");
    }
    sb.append("{ throw new java.lang.IllegalArgumentException(q); }\n");
    sb.append("}\n");
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
				return this.translateComparison(valueExpr, valueType, atom.attributeList(), atom, atom.op(), ctxt);
			} else {
				return this.translateTrait(valueExpr, valueType, atom.attributeList(), atom.target(), atom.is(), ctxt);
			}
		}
	}

	public static CompiledQuery compile(final String string) throws FileNotFoundException, IOException, ParseException, TokenMgrError {
		final List<Query> prog = parseProgram(string);
		final File f = compileProgram(prog);
		final Map<String, Set<String>> exprExists = new HashMap<>();
		final Map<String, Set<String>> stmtExists = new HashMap<>();
		
		final Map<String, Set<String>> exprSum = new HashMap<>();
		final Map<String, Set<String>> stmtSum = new HashMap<>();
		
		for(final Query q : prog) {
			Map<String, Set<String>> container;
			if(q.target() == QueryTarget.STATEMENT) {
				if(q.metric() == Metric.SUM) {
					container = stmtSum;
				} else {
					container = stmtExists;
				}
			} else {
				if(q.metric() == Metric.SUM) {
					container = exprSum;
				} else {
					container = exprExists;
				}
			}
			if(!container.containsKey(q.name())) {
				container.put(q.name(), new HashSet<String>());
			}
			if(q.deriving() != null) {
				if(!container.containsKey(q.deriving())) {
					container.put(q.deriving(), new HashSet<String>());
				}
				container.get(q.deriving()).add(q.name());
			}
		}
		final File assembledJar = assembleJarFile(f);
		return new CompiledQuery(assembledJar, new QueryTree(exprExists, stmtExists, exprSum, stmtSum), "codestats.QueryInterpreterImpl");
	}

	private static File assembleJarFile(final File interpreterClass) throws FileNotFoundException, IOException {
		final File jar = File.createTempFile("codestatsAssemble", ".jar");
		jar.deleteOnExit();
		try(JarOutputStream jarOutput = new JarOutputStream(new FileOutputStream(jar))) {
			for(final File f : interpreterClass.getParentFile().listFiles()) {
				if(!f.getName().endsWith(".class")) {
					continue;
				}
				final JarEntry interpEntry = new JarEntry(f.toString().replaceFirst("^(.+)codestats/", "codestats/"));
				interpEntry.setTime(System.currentTimeMillis());
				jarOutput.putNextEntry(interpEntry);
				IOUtils.copy(new FileInputStream(f), jarOutput);
				jarOutput.closeEntry();
			}
		}
		return jar;
	}

	private static List<Query> parseProgram(final String string) throws ParseException, TokenMgrError {
		final List<Query> parsedProgram = QueryParser.parse(new ByteArrayInputStream(string.getBytes()));
		final List<Query> toReturn = new ArrayList<>();
		for(final Query q : parsedProgram) {
			if(q.metric() == Metric.HYBRID) {
				toReturn.add(new Query(q.name() + "$SUM", q.deriving() == null ? null : q.deriving() + "$SUM", Metric.SUM, q.target(), q.getPredicate()));
				toReturn.add(new Query(q.name() + "$EXISTS", q.deriving() == null ? null : q.deriving() + "$EXISTS", Metric.EXISTS, q.target(), q.getPredicate()));
			} else {
				toReturn.add(q);
			}
		}
		return toReturn;
	}
}
