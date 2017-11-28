package edu.washington.cse.codestats;

import java.util.List;


public class PredicateAtom {
	private final boolean is;
	private final String target;
	private final String op;
	private final Type type;
	private final List<String> attributes;

	public static enum Type {
		TRAIT_CHECK,
		ATTRIBUTE_CHECK
	}
	
	public boolean is() { return this.is; }
	public String target() { return this.target; }
	public String op() { return this.op; }
	public Type type() { return this.type; }
	public List<String> attributeList() { return this.attributes; }
}
