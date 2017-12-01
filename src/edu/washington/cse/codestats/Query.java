package edu.washington.cse.codestats;

public class Query {
	private final Metric metric;
	private final PredicateMirror predicate;
	private final String deriving;
	private final String name;
	private final QueryTarget target;

	public Metric metric() {
		return metric;
	}

	public PredicateMirror getPredicate() {
		return predicate;
	}

	public String deriving() {
		return deriving;
	}

	public String name() {
		return name;
	}

	public QueryTarget target() {
		return target;
	}

	public Query(final String name, final String deriving, final Metric metric, final QueryTarget target, final PredicateMirror predicate) {
		this.metric = metric;
		this.predicate = predicate;
		this.deriving = deriving;
		this.name = name;
		this.target = target;
	}

	@Override
	public String toString() {
		return "Query [metric=" + metric + ", predicate=" + predicate + ", deriving=" + deriving + ", name=" + name + ", target=" + target + "]";
	}
}
