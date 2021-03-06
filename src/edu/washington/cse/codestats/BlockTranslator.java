package edu.washington.cse.codestats;

import edu.washington.cse.codestats.Compiler.CompileContext;

public class BlockTranslator implements Translator {
	private static int ID_COUNTER = 0; 

	private final String inputType;
	private final String returnType;
	private final String methodBody;
	private final String outputType;

	private final int id;

	public BlockTranslator(final String inputType, final String returnType, final String methodBody, final String outputType) {
		this.inputType = inputType;
		this.returnType = returnType;
		this.methodBody = methodBody;
		this.outputType = outputType;
		this.id = ID_COUNTER++;
	}

	@Override
	public String getOutputType() {
		return outputType;
	}

	@Override
	public String translate(final String casted, final String containingType, final String currAttr, final CompileContext ctxt, final String contextName) {
		final String methodName = String.format("%s_%s_%d", containingType, currAttr, id);
		final String substBody = methodBody.replaceAll("\\{0\\}", "arg").replaceAll("\\{1\\}", "CONTEXT");
		final String helperMethod = String.format("public %s %s(%s arg, %s CONTEXT) { %s }", returnType, methodName, inputType, QueryContext.class.getName(), substBody);
		ctxt.addUtilityMethod(methodName, helperMethod);
		return String.format("%s(%s, %s)", methodName, casted, contextName);
	}

}
