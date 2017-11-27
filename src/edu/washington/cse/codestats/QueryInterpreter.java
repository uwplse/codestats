package edu.washington.cse.codestats;

import soot.Value;
import soot.jimple.Stmt;

public interface QueryInterpreter {
	public boolean interpret(String query, Value v);
	public boolean interpret(String query, Stmt v);
}
