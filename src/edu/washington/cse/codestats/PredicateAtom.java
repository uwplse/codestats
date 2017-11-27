package edu.washington.cse.codestats;

import java.util.List;


public interface PredicateAtom {
	public static enum Type {
		TRAIT_CHECK,
		ATTRIBUTE_CHECK
	}
	
	public boolean is();
	public String target();
	public String op();
	public Type type();
	public List<String> attributeList();
}
