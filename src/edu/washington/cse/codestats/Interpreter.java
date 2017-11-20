package edu.washington.cse.codestats;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import soot.Body;
import soot.PatchingChain;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Expr;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import edu.washington.cse.codestats.ExpressionPredicateMirror.PredicateType;

public class Interpreter {
	public final static Map<String, Integer> EXISTS = new HashMap<>();
	public final static Map<String, Integer> NOT_EXISTS = new HashMap<>();
	static {
		EXISTS.put("HAS", 1);
		EXISTS.put("TOTAL", 1);
	}
	
	static {
		NOT_EXISTS.put("TOTAL", 1);
	}
	
	private final ProgramMirror pm;

	public Interpreter(final ProgramMirror pm) {
		this.pm = pm;
	}
	
	public Map<String, Integer> interpret(final SootMethod m) {
		if(pm.getMetric() == Metric.EXISTS) {
			return this.interpretExists(m);
		} else {
			return this.interpretSum(m);
		}
	}

	private Map<String, Integer> interpretSum(final SootMethod m) {
		final SumMirror sm = pm.getRootSum();
		// TODO Auto-generated method stub
		return null;
	}

	private Map<String, Integer> interpretExists(final SootMethod m) {
		final ExistsMirror em = pm.getExistsFragment();
		if(em.getTarget() == QueryTarget.EXPRESSION) {
			for(final Expr e : this.iterateExpressions(m.retrieveActiveBody().getUnits())) {
				if(this.matches(e, em.getExpressionPredicate())) {
					return EXISTS;
				}
			}
		} else if(em.getTarget() == QueryTarget.STATEMENT) {
			final Body b = m.retrieveActiveBody();
			for(final Unit u : b.getUnits()) {
				final Stmt s = (Stmt)u;
				if(this.matches(s, em.getStatementPredicate())) {
					return EXISTS;
				}
			}
		}
		return NOT_EXISTS;
	}

	private boolean matches(final Expr e, final ExpressionPredicateMirror expressionPredicate) {
		if(expressionPredicate.getType() == PredicateType.ATOM) {
			final ExpressionPredicateAtom epa = (ExpressionPredicateAtom) expressionPredicate;
			switch(epa.getAtomType()) {
			case ATTRIBUTE_CHECK:
				return interpretExpressionAttribute(e, epa);
			case CONSTANT_CHECK:
				return e instanceof soot.jimple.Constant;
			case NOT_CHECK:
				return !(matches(e, epa.getSubPredicate()));
			case NULL_CHECK:
				return e instanceof NullConstant;
			case RUNTIME_TYPE_CHECK:
				return e.getType().toString().equals(epa.getRuntimeType());
			default:
				throw new RuntimeException("Unhandled predicate");
			}
		} else if(expressionPredicate.getType() == PredicateType.AND) {
			@SuppressWarnings("unchecked")
			final List<ExpressionPredicateMirror> children = ((CompoundPredicate<ExpressionPredicateMirror>)expressionPredicate).getChildren();
			for(final ExpressionPredicateMirror c : children) {
				if(!matches(e, c)) {
					return false;
				}
			}
			return true;
		} else {
			assert expressionPredicate.getType() == PredicateType.OR;
			@SuppressWarnings("unchecked")
			final List<ExpressionPredicateMirror> children = ((CompoundPredicate<ExpressionPredicateMirror>)expressionPredicate).getChildren();
			for(final ExpressionPredicateMirror c : children) {
				if(matches(e, c)) {
					return true;
				}
			}
			return false;
		}
	}

	private boolean interpretExpressionAttribute(final Expr e, final ExpressionPredicateAtom epa) {
		final List<String> attr = epa.getAttributeChain();
		if(epa.getSubPredicate() == null) {
			assert epa.getCheckOp() != null;
			final Collection<Constant> constAttrs = collectConstantAttr(e, attr);
			for(final Constant c : constAttrs) {
				if(!compare(c, epa.getAttributeTarget())) {
					return false;
				}
			}
		} else {
			for(final Expr subExpr : collectSubExprAttr(e, attr)) {
				if(!interpretExpressionAttribute(subExpr, epa.getSubPredicate())) {
					return false;
				}
			}
		}
		return true;
	}

	private Iterable<Expr> collectSubExprAttr(final Expr e, final List<String> attr) {
		return collectSubExprAttr(Collections.<Attributable>singleton(new ExprAttributes(e)), attr);
	}

	private Iterable<Expr> collectSubExprAttr(final Set<Attributable> toInterp, final List<String> attr) {
		if(attr.size() == 1) {
			final Set<Expr> nextLevel = new HashSet<>();
			for(final Attributable e : toInterp) {
				nextLevel.addAll(e.getExpr(attr.get(0)));
			}
			return nextLevel;
		} else {
			final Set<Attributable> nextLevel = new HashSet<>();
			for(final Attributable e : toInterp) {
				nextLevel.addAll(e.getSub(attr.get(0)));
			}
			return collectSubExprAttr(nextLevel, attr.subList(1, attr.size()));
		}
	}

	private boolean compare(final Constant lhs, final Constant rhs) {
		// TODO Auto-generated method stub
		return false;
	}

	private Set<Constant> collectConstantAttr(final Expr e, final List<String> attr) {
		return collectConstantAttr(Collections.<Attributable>singleton(new ExprAttributes(e)), attr);
	}

	private Set<Constant> collectConstantAttr(final Set<Attributable> toCollect, final List<String> attr) {
		if(attr.size() == 1) {
			final Set<Constant> toReturn = new HashSet<>();
			for(final Attributable a : toCollect) {
				toReturn.add(a.getConstant(attr.get(0)));
			}
			return toReturn;
		} else {
			final Set<Attributable> toReturn = new HashSet<>();
			for(final Attributable a : toCollect) {
				toReturn.addAll(a.getSub(attr.get(0)));
			}
			return collectConstantAttr(toReturn, attr.subList(1, attr.size()));
		}
	}

	private Iterable<Expr> iterateExpressions(final PatchingChain<Unit> units) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean matches(final Stmt s, final StatementPredicateMirror statementPredicate) {
		// TODO Auto-generated method stub
		return false;
	}
}
