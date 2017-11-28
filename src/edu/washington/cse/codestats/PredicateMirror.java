package edu.washington.cse.codestats;

import java.util.List;

public class PredicateMirror {
	public static enum PredicateType {
		AND,
		OR,
		ATOM
	}

	private final PredicateType type;
	private final List<PredicateMirror> children;
	private final PredicateAtom atom;
	
	public PredicateType getType() { return this.type; }
	public List<PredicateMirror> childPredicates() { return this.children; }
	public PredicateAtom atom() { return this.atom; }

	public PredicateMirror(PredicateType type, List<PredicateMirror> children) {
		this.type = type;
		this.children = children;
		this.atom = null;
	}

	public PredicateMirror(PredicateAtom atom) {
		this.type = PredicateType.ATOM;
		this.children = null;
		this.atom = atom;
	}
}
