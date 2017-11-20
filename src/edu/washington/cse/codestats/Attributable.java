package edu.washington.cse.codestats;

import java.util.Collection;

import soot.jimple.Expr;

public interface Attributable {

	Collection<? extends Attributable> getSub(String string);

	Constant getConstant(String string);

	Collection<? extends Expr> getExpr(String string);

}
