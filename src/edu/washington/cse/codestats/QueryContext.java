package edu.washington.cse.codestats;

import java.util.Collection;

import soot.Local;
import soot.SootMethodRef;

public interface QueryContext {
	public SootMethodRef getContainingMethod();
	public Local getThisLocal();
	public Collection<Local> getArgLocals();
}
