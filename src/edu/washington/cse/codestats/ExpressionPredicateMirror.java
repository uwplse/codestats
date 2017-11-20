package edu.washington.cse.codestats;

public interface ExpressionPredicateMirror {
	public static enum PredicateType {
		AND,
		OR,
		ATOM
	}
	
	public PredicateType getType();
}
