package edu.washington.cse.codestats;

public interface Query {
	PredicateMirror getPredicate();
	String deriving();
	String name();
	QueryTarget target();
	Metric metric();
}
