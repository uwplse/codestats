package edu.washington.cse.codestats;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.io.Writable;

import soot.ValueBox;
import soot.jimple.Stmt;
import edu.washington.cse.codestats.shadow.EXISTS;
import edu.washington.cse.codestats.shadow.SUM;

public class QueryTree implements Writable {
	public static class QueryGroup<TARGET, METRIC> {
		private final Iterable<String> roots;
		private final Map<String, Set<String>> tree;
		
		private QueryGroup(final Iterable<String> roots, final Map<String, Set<String>> tree) {
			this.roots = roots;
			this.tree = tree;
		}
		
		public Iterable<String> roots() {
			return roots;
		}
		
		public Iterable<String> children(final String s) {
			return tree.containsKey(s) ? tree.get(s) : Collections.<String>emptySet();
		}
	}
	
	private final Map<String, Set<String>> exprExists;
	private final Map<String, Set<String>> stmtExists;
	private final Map<String, Set<String>> exprSum;
	private final Map<String, Set<String>> stmtSum;

	private final Set<String> sumExprRoots;
	private final Set<String> sumStmtRoots;
	private final Set<String> existsExprRoots;
	private final Set<String> existsStmtRoots;
	private QueryGroup<ValueBox, SUM> esQG;
	private QueryGroup<ValueBox, EXISTS> eeQG;
	private QueryGroup<Stmt, SUM> ssQG;
	private QueryGroup<Stmt, EXISTS> seQG;
	
	public QueryTree() {
		exprExists = new HashMap<>();
		stmtExists = new HashMap<>();
		exprSum = new HashMap<>();
		stmtSum = new HashMap<>();
		
		sumExprRoots = new HashSet<>();
		sumStmtRoots = new HashSet<>();
		existsExprRoots = new HashSet<>();
		existsStmtRoots = new HashSet<>();
	}
	
	public QueryTree(final Map<String, Set<String>> exprExists, final Map<String, Set<String>> stmtExists,
			final Map<String, Set<String>> exprSum, final Map<String, Set<String>> stmtSum) {
		this.exprExists = exprExists;
		this.stmtExists = stmtExists;
		this.exprSum = exprSum;
		this.stmtSum = stmtSum;
		
		existsExprRoots = getRoots(exprExists);
		existsStmtRoots = getRoots(stmtExists);
		sumExprRoots = getRoots(exprSum);
		sumStmtRoots = getRoots(stmtSum);
		
		mkCache();
	}

	private Set<String> getRoots(final Map<String, Set<String>> queryMap) {
		final HashSet<String> roots = new HashSet<>(queryMap.keySet());
		for(final Set<String> sub : queryMap.values()) {
			roots.removeAll(sub);
		}
		return roots;
	}

	@Override
	public void write(final DataOutput out) throws IOException {
		for(final Set<String> root : this.getSerializeRoots()) {
			this.writeSet(root, out);
		}
		for(final Map<String, Set<String>> queryTree : this.getSerializeQueries()) {
			this.writeMap(queryTree, out);
		}
	}

	private void writeMap(final Map<String, Set<String>> queryTree, final DataOutput out) throws IOException {
		out.writeInt(queryTree.size());
		for(final Map.Entry<String, Set<String>> kv : queryTree.entrySet()) {
			out.writeUTF(kv.getKey());
			writeSet(kv.getValue(), out);
		}
	}

	private void writeSet(final Set<String> root, final DataOutput out) throws IOException {
		out.writeInt(root.size());
		for(final String elem : root) {
			out.writeUTF(elem);
		}
	}
	
	@SuppressWarnings("unchecked")
	private Set<String>[] getSerializeRoots() {
		return new Set[]{
			existsExprRoots,
			existsStmtRoots,
			sumExprRoots,
			sumStmtRoots,
		};
	}

	@SuppressWarnings("unchecked")
	private Map<String, Set<String>>[] getSerializeQueries() {
		return new Map[]{
			exprSum,
			stmtSum,
			exprExists,
			stmtExists,
		};
	}

	@Override
	public void readFields(final DataInput in) throws IOException {
		for(final Set<String> root : this.getSerializeRoots()) {
			this.readSet(root, in);
		}
		for(final Map<String, Set<String>> queryTree : this.getSerializeQueries()) {
			this.readMap(queryTree, in);
		}
		mkCache();
	}

	private void readMap(final Map<String, Set<String>> queryTree, final DataInput in) throws IOException {
		final int sz = in.readInt();
		for(int i = 0; i < sz; i++) {
			final String key = in.readUTF();
			final Set<String> value = new HashSet<>();
			this.readSet(value, in);
			queryTree.put(key, value);
		}
	}

	private void readSet(final Set<String> root, final DataInput in) throws IOException {
		final int sz = in.readInt();
		for(int i = 0; i < sz; i++) {
			root.add(in.readUTF());
		}
	}
	
	private void mkCache() {
		esQG = new QueryGroup<>(sumExprRoots, exprSum);
		eeQG = new QueryGroup<>(existsExprRoots, exprExists);
		ssQG = new QueryGroup<>(sumStmtRoots, stmtSum);
		seQG = new QueryGroup<>(existsStmtRoots, stmtExists);
	}
	
	public QueryGroup<ValueBox, SUM> getExprSums() {
		return esQG;
	}
	
	public QueryGroup<ValueBox, EXISTS> getExprExists() {
		return eeQG;
	}
	
	public QueryGroup<Stmt, SUM> getStmtSums() {
		return ssQG;
	}
	
	public QueryGroup<Stmt, EXISTS> getStmtExists() {
		return seQG;
	}
	
	public boolean hasExprQueries() {
		return !this.existsExprRoots.isEmpty() || !this.sumExprRoots.isEmpty();
	}
}
