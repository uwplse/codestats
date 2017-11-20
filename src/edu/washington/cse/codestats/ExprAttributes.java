package edu.washington.cse.codestats;

import java.util.Collection;

import soot.grimp.internal.GNewInvokeExpr;
import soot.jimple.ArrayRef;
import soot.jimple.CastExpr;
import soot.jimple.Expr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.internal.AbstractBinopExpr;
import soot.jimple.internal.AbstractUnopExpr;

public class ExprAttributes implements Attributable {

	private final Expr e;

	public ExprAttributes(final Expr e) {
		this.e = e;
	}

	@Override
	public Collection<? extends Attributable> getSub(final String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Constant getConstant(final String attrName) {
		switch(attrName) {
		case "type":
		{
			if(e instanceof ArrayRef) {
				return Constant.of("ArrayRead");
			} else if(e instanceof InstanceFieldRef) {
				return Constant.of("InstanceField");
			} else if(e instanceof StaticFieldRef) {
				return Constant.of("StaticField");
			} else if(e instanceof GNewInvokeExpr) {
				return Constant.of("New");
			} else if(e instanceof AbstractBinopExpr) {
				return Constant.of("Binop");
			} else if(e instanceof CastExpr) {
				return Constant.of("Cast");
			} else if(e instanceof AbstractUnopExpr) {
				return Constant.of("Unop");
			} else if(e instanceof StaticInvokeExpr) {
				return Constant.of("StaticInvoke");
			} else if(e instanceof InstanceInvokeExpr) {
				return Constant.of("InstanceInvoke");
			} else if(e instanceof soot.jimple.Constant) {
				return Constant.of("Constant");
			}
		}
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<? extends Expr> getExpr(final String string) {
		// TODO Auto-generated method stub
		return null;
	}

}
