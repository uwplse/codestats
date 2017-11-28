package edu.washington.cse.codestats.hadoop;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.grimp.Grimp;
import soot.grimp.GrimpBody;
import soot.jimple.Stmt;
import soot.options.Options;
import edu.washington.cse.codestats.QueryInterpreter;

public class StatMapper extends Mapper<Text, BytesWritable, Text, LongWritable> {
	public final static String INTERPRETER_CLASS_NAME = "codestats.interpreter.class";
	private QueryInterpreter interpreter;
	public final static String STMT_SUM = "codestats.sum.stmt";
	public final static String EXPR_SUM = "codestats.sum.expr";
	public final static String STMT_EXISTS = "codestats.exists.stmt";
	public final static String EXPR_EXISTS = "codestats.exists.expr";
	
	private final Map<String, Set<String>> stmtExists = new HashMap<>();
	private final Map<String, Set<String>> exprExists = new HashMap<>();
	private final Map<String, Set<String>> exprSums = new HashMap<>();
	private final Map<String, Set<String>> stmtSums = new HashMap<>();
	
	private final Set<String> sumExprRoots = new HashSet<>();
	private final Set<String> sumStmtRoots = new HashSet<>();
	private final Set<String> existsExprRoots = new HashSet<>();
	private final Set<String> existsStmtRoots = new HashSet<>();
	
	private final Text outputValue = new Text();
	private final LongWritable oneValue = new LongWritable(1L);
	
	@Override
	protected void setup(final Context context) throws IOException, InterruptedException {
		super.setup(context);
		try {
			interpreter = (QueryInterpreter) Thread.currentThread().getContextClassLoader().loadClass(context.getConfiguration().get(INTERPRETER_CLASS_NAME)).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new IOException(e);
		}
		
		calculateQueryTree(context, STMT_SUM, stmtSums, sumStmtRoots);
		calculateQueryTree(context, STMT_EXISTS, stmtExists, existsStmtRoots);
		
		calculateQueryTree(context, EXPR_SUM, exprSums, sumExprRoots);
		calculateQueryTree(context, EXPR_EXISTS, exprExists, existsExprRoots);
	}

	private void calculateQueryTree(final Context context, final String property, final Map<String, Set<String>> queryTree, final Set<String> queryRoots) {
		final Set<String> notRoot = new HashSet<>();
		if(context.getConfiguration().get(property, "").isEmpty()) {
			return;
		}
		for(final String s : context.getConfiguration().get(property).split(";")) {
			final String[] kv = s.split("=");
			final String target = kv[0];
			queryTree.put(target, new HashSet<String>());
			if(kv.length == 2) {
				for(final String derivedQueries : kv[1].split(",")) {
					queryTree.get(target).add(derivedQueries);
					notRoot.add(derivedQueries);
					queryRoots.remove(derivedQueries);
				}
			}
			if(!notRoot.contains(target)) {
				queryRoots.add(target);
			}
		}
	}
	
	@Override
	protected void map(final Text key, final BytesWritable value, final Context context) throws IOException, InterruptedException {
		G.reset();
		final String clsName = key.toString();
		final String classFileName = clsName.replace('.', '/') + ".class";
		SourceLocator.v().additionalClassLoader(new ClassLoader() {
			@Override
			public InputStream getResourceAsStream(final String clsName) {
				if(clsName.equals(classFileName)) {
					return new ByteArrayInputStream(value.getBytes(), 0, value.getLength());
				} else {
					return null;
				}
			}
		});
		Scene.v().addBasicClass(clsName);
		Options.v().set_allow_phantom_refs(true);
		Scene.v().loadBasicClasses();
		final SootClass cls = Scene.v().loadClass(clsName, SootClass.BODIES);
		if(cls.isInterface()) {
			return;
		}
		for(final SootMethod m : cls.getMethods()) {
			final HashSet<String> stmtExists = new HashSet<>();
			final HashSet<String> exprExists = new HashSet<>();
			if(!m.isConcrete()) {
				continue;
			}
			final GrimpBody body = Grimp.v().newBody(m.retrieveActiveBody(), "gb");
			PackManager.v().getPack("gop").apply(body);
			for(final Unit u : body.getUnits()) {
				final Stmt s = (Stmt) u;
				for(final String existsRoot : existsStmtRoots) {
					interpretStmtExists(s, existsRoot, stmtExists);
				}
				for(final String sumRoot : sumStmtRoots) {
					interpretStmtSum(s, sumRoot, context);
				}
				if(!exprSums.isEmpty() || !exprExists.isEmpty()) {
					for(final ValueBox vb : s.getUseBoxes()) {
						for(final String sumRoot : sumExprRoots) {
							interpretExprSum(vb.getValue(), sumRoot, context);
						}
						for(final String existsRoot : existsExprRoots) {
							interpretExprExists(vb.getValue(), existsRoot, exprExists);
						}
					}
				}
			}
			for(final String foundPattern : exprExists) {
				writeQueryIncrement(foundPattern, context);
			}
			for(final String foundPattern : stmtExists) {
				writeQueryIncrement(foundPattern, context);
			}
			writeQueryIncrement("$NUM_METHODS", context);
		}
	}

	private void interpretExprExists(final Value value, final String query, final HashSet<String> exists) {
		if(interpreter.interpret(query, value)) {
			exists.add(query);
			for(final String derivedQuery : exprExists.get(query)) {
				interpretExprExists(value, derivedQuery, exists);
			}
		}
	}

	private void interpretExprSum(final Value value, final String query, final Context context) throws IOException, InterruptedException {
		if(interpreter.interpret(query, value)) {
			writeQueryIncrement(query, context);
			for(final String derivedQuery : exprSums.get(query)) {
				interpretExprSum(value, derivedQuery, context);
			}
		}
	}

	private void writeQueryIncrement(final String query, final Context context) throws IOException, InterruptedException {
		outputValue.set(query);
		context.write(outputValue, oneValue);
	}

	private void interpretStmtExists(final Stmt s, final String query, final HashSet<String> existsSet) {
		if(interpreter.interpret(query, s)) {
			existsSet.add(query);
			for(final String derivedQuery : stmtExists.get(query)) {
				interpretStmtExists(s, derivedQuery, existsSet);
			}
		}
	}
	
	private void interpretStmtSum(final Stmt s, final String query, final Context c) throws IOException, InterruptedException {
		if(interpreter.interpret(query, s)) {
			writeQueryIncrement(query, c);
			for(final String derivedQuery : stmtSums.get(query)) {
				interpretStmtSum(s, derivedQuery, c);
			}
		}
	}
}
