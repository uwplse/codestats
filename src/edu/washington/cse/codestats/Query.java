package edu.washington.cse.codestats;

public class Query {
    private final Metric metric;
    private final PredicateMirror predicate;
    private final String deriving;
    private final String name;
    private final QueryTarget target;

	public Metric metric() { return metric; }
    public PredicateMirror getPredicate() { return predicate; }
    public String deriving() { return deriving; }
    public String name() { return name; }
    public QueryTarget target() { return target; }

    public Query(String name, String deriving, Metric metric,
                 QueryTarget target, PredicateMirror predicate) {
	    this.metric = metric;
        this.predicate = predicate;
        this.deriving = deriving;
        this.name = name;
        this.target = target;
    }
}
