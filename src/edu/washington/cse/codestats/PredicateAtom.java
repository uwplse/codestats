package edu.washington.cse.codestats;

import java.util.List;

public class PredicateAtom {
	private final boolean is;
	private final String target;
	private final String operator;
	private final Type type;
	private final List<String> attribute;

	public static enum Type {
		TRAIT_CHECK, ATTRIBUTE_CHECK
	}

	public boolean is() {
		return this.is;
	}

	public String target() {
		return this.target;
	}

	public String op() {
		return this.operator;
	}

	public Type type() {
		return this.type;
	}

	public List<String> attributeList() {
		return this.attribute;
	}

	public PredicateAtom(final List<String> attribute, final String operator, final String target) {
		this.is = false;
		this.attribute = attribute;
		this.operator = operator;
		this.target = target;
		this.type = Type.ATTRIBUTE_CHECK;
	}

	public PredicateAtom(final List<String> attribute, final boolean is, final String target) {
		this.operator = null;
		this.attribute = attribute;
		this.is = is;
		this.target = target;
		this.type = Type.TRAIT_CHECK;
	}

	@Override
	public String toString() {
		return "PredicateAtom [is=" + is + ", target=" + target + ", operator=" + operator + ", type=" + type + ", attribute=" + attribute + "]";
	}
}
