package edu.washington.cse.codestats;

public interface ExistsMirror {

	QueryTarget getTarget();
	StatementPredicateMirror getStatementPredicate();
	ExpressionPredicateMirror getExpressionPredicate();

}
