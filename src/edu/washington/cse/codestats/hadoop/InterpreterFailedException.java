package edu.washington.cse.codestats.hadoop;

public class InterpreterFailedException extends RuntimeException {
	public InterpreterFailedException(final Exception e) {
		super(e);
	}
}
