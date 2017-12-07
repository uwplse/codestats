package edu.washington.cse.codestats;

import soot.ValueBox;
import soot.jimple.Stmt;

public interface QueryInterpreter {
	public boolean interpret(String query, ValueBox v);
	public boolean interpret(String query, Stmt v);
}
