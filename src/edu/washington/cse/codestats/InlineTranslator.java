package edu.washington.cse.codestats;

import edu.washington.cse.codestats.Compiler.CompileContext;

public class InlineTranslator implements Translator {

	private final String valueType;
	private final String templateString;

	public InlineTranslator(final String templateString, final String valueType) {
		this.templateString = templateString;
		this.valueType = valueType;
	}

	public InlineTranslator(final String templateString) {
		this.templateString = templateString;
		this.valueType = null;
	}

	@Override
	public String getOutputType() {
		return valueType;
	}

	@Override
	public String translate(final String casted, final String containingType, final String currAttr, final CompileContext ctxt) {
		return templateString.replaceAll("\\{0\\}", casted);
	}

}
