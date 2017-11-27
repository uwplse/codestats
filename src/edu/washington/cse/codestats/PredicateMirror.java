package edu.washington.cse.codestats;

import java.util.List;

public interface PredicateMirror {
	public static enum PredicateType {
		AND,
		OR,
		ATOM
	}
	
	public PredicateType getType();
	public List<PredicateMirror> childPredicates();
	public PredicateAtom atom();
}
