package edu.washington.cse.codestats;

import edu.washington.cse.codestats.Compiler.CompileContext;

public interface Translator {

	String getOutputType();

	String translate(String casted, String containingType, String currAttr, CompileContext ctxt);

}
