package edu.washington.cse.codestats;

public interface ProgramMirror {

	Metric getMetric();
	ExistsMirror getExistsFragment();
	SumMirror getRootSum();

}
