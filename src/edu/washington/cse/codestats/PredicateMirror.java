package edu.washington.cse.codestats;

import java.util.List;

public class PredicateMirror {
	public static enum PredicateType {
		AND, OR, ATOM
	}

	private final PredicateType type;
	private final List<PredicateMirror> children;
	private final PredicateAtom atom;

	public PredicateType getType() {
		return this.type;
	}

	public List<PredicateMirror> childPredicates() {
		return this.children;
	}

	public PredicateAtom atom() {
		return this.atom;
	}

	public PredicateMirror(final PredicateType type, final List<PredicateMirror> children) {
		this.type = type;
		this.children = children;
		this.atom = null;
	}

	public PredicateMirror(final PredicateAtom atom) {
		this.type = PredicateType.ATOM;
		this.children = null;
		this.atom = atom;
	}

	@Override
	public String toString() {
		return "PredicateMirror [type=" + type + ", children=" + children + ", atom=" + atom + "]";
	}
}
