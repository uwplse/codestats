package edu.washington.cse.codestats;

import java.util.List;

public interface ExpressionPredicateAtom extends ExpressionPredicateMirror {
	public static enum Type {
		RUNTIME_TYPE_CHECK,
		CONSTANT_CHECK,
		NULL_CHECK,
		NOT_CHECK,
		ATTRIBUTE_CHECK
	}
	
	public Type getAtomType();
	public ExpressionPredicateAtom getSubPredicate();
	
	public List<String> getAttributeChain();
	
	public Constant getAttributeTarget();
	public String getCheckOp();
	
	public String getRuntimeType();
	
}
