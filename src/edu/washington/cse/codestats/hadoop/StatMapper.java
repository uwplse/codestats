package edu.washington.cse.codestats.hadoop;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import soot.ClassProvider;
import soot.ClassSource;
import soot.FoundFile;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SourceLocator;
import soot.Unit;
import soot.ValueBox;
import soot.asm.AsmClassProvider;
import soot.grimp.Grimp;
import soot.grimp.GrimpBody;
import soot.jimple.Stmt;
import soot.options.Options;
import soot.tagkit.AttributeValueException;
import soot.tagkit.Tag;
import edu.washington.cse.codestats.CompiledQuery;
import edu.washington.cse.codestats.QueryInterpreter;
import edu.washington.cse.codestats.QueryTree;
import edu.washington.cse.codestats.QueryTree.QueryGroup;
import edu.washington.cse.codestats.shadow.EXISTS;
import edu.washington.cse.codestats.shadow.SUM;
import fj.F2;
import fj.function.Effect1;

public class StatMapper extends Mapper<Text, BytesWritable, Text, LongWritable> {
	private static final String FAILED_METHOD_KEY = "$FAILED_METHODS";

	private static enum Diagnostics {
		OOM_METHOD,
		FAILED_METHODS,
		FAILED_CLASSES,
	}
	
	public final static String INTERPRETER_CLASS_NAME = "codestats.interpreter.class";
	private QueryInterpreter interpreter;
	public final QueryTree qt = new QueryTree();
	
	private final Text outputValue = new Text();
	private static final LongWritable ONE_VALUE = new LongWritable(1L);
	private final LongWritable count = new LongWritable();
	private Constructor<?> classSourceConstructor;
	private CountManager cm;
	
	@Override
	protected void setup(final Context context) throws IOException, InterruptedException {
		super.setup(context);
		try {
			interpreter = (QueryInterpreter) Thread.currentThread().getContextClassLoader().loadClass(context.getConfiguration().get(INTERPRETER_CLASS_NAME)).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new IOException(e);
		}
		
		CompiledQuery.readTree(context.getConfiguration(), qt);
		cm = new CountManager(); 
		
		try {
			classSourceConstructor = Class.forName("soot.asm.AsmClassSource").getDeclaredConstructors()[0];
			classSourceConstructor.setAccessible(true);
		} catch (final ClassNotFoundException e) {
			throw new IOException();
		}
	}
	
	private final F2<String, Stmt, Boolean> stmtQuery = new F2<String, Stmt, Boolean>() {
		@Override
		public Boolean f(final String a, final Stmt b) {
			return interpreter.interpret(a, b);
		}
	};
	
	private final F2<String, ValueBox, Boolean> exprQuery = new F2<String, ValueBox, Boolean>() {
		@Override
		public Boolean f(final String a, final ValueBox b) {
			return interpreter.interpret(a, b);
		}
	};
	
	private interface MatchEffect<T, M> extends Effect1<String> { }
	
	private class CountManager {
		private final Map<String, Integer> exprCounts = new HashMap<>();
		private final Map<String, Integer> stmtCounts = new HashMap<>();
		private final Set<String> foundStmts = new HashSet<>();
		private final Set<String> foundExpr = new HashSet<>();
		
		private final MatchEffect<ValueBox, SUM> eSumIncr_  = new MatchEffect<ValueBox, SUM>() {
			@Override
			public void f(final String a) {
				if(exprCounts.containsKey(a)) {
					exprCounts.put(a, exprCounts.get(a) + 1);
				} else {
					exprCounts.put(a, 1);
				}
			}
		};
		
		private final MatchEffect<Stmt, SUM> sSumIncr_  = new MatchEffect<Stmt, SUM>() {
			@Override
			public void f(final String a) {
				if(stmtCounts.containsKey(a)) {
					stmtCounts.put(a, stmtCounts.get(a) + 1);
				} else {
					stmtCounts.put(a, 1);
				}
			}
		};
		
		private final MatchEffect<Stmt, EXISTS> sFound = new MatchEffect<Stmt, EXISTS>() {
			@Override
			public void f(final String a) {
				foundStmts.add(a);
			}
		};
		
		private final MatchEffect<ValueBox, EXISTS> eFound = new MatchEffect<ValueBox, EXISTS>() {
			@Override
			public void f(final String a) {
				foundExpr.add(a);
			}
		};
		
		public MatchEffect<ValueBox, SUM> exprSumIncrementer() {
			return eSumIncr_;
		}
		
		public MatchEffect<ValueBox, EXISTS> exprFoundFlag() {
			return eFound;
		}
		
		public MatchEffect<Stmt, SUM> stmtSumIncrementer() {
			return sSumIncr_;
		}
		
		public MatchEffect<Stmt, EXISTS> stmtFoundFlag() {
			return sFound;
		}
		
		public void reset() { 
			exprCounts.clear();
			stmtCounts.clear();
			foundStmts.clear();
			foundExpr.clear();
		}
	}

	@Override
	protected void map(final Text key, final BytesWritable value, final Context context) throws IOException, InterruptedException {
		cm.reset();
		try {
			G.reset();
			final String clsName = key.toString();
			final List<ClassProvider> cp = new ArrayList<>();
			cp.add(new AsmClassProvider());
			cp.add(new ClassProvider() {
				@Override
				public ClassSource find(final String className) {
					if(className.equals(clsName)) {
						final FoundFile f = new FoundFile(new File("DOES_NOT_ACTUALLY_EXIST")) {
							private ByteArrayInputStream baos;
	
							@Override
							public InputStream inputStream() {
								baos = new ByteArrayInputStream(value.getBytes(), 0, value.getLength());
								return baos;
							}
							
							@Override
							public void close() { 
								if(baos != null) {
									try {
										baos.close();
									} catch (final IOException e) { }
									baos = null;
								}
							}
						};
						try {
							return (ClassSource) classSourceConstructor.newInstance(className, f);
						} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							return null;
						}
					} else {
						return null;
					}
				}
			});
			SourceLocator.v().setClassProviders(cp);
			Scene.v().addBasicClass(clsName);
			Options.v().set_allow_phantom_refs(true);
			Scene.v().loadBasicClasses();
			final SootClass cls = Scene.v().loadClass(clsName, SootClass.BODIES);
			if(cls.isInterface()) {
				return;
			}
			for(final SootMethod m : cls.getMethods()) {
				try {
					if(!m.isConcrete()) {
						continue;
					}
					final GrimpBody body = Grimp.v().newBody(m.retrieveActiveBody(), "gb");
					m.releaseActiveBody();
					PackManager.v().getPack("gop").apply(body);
					final Tag hostTag = new Tag() {
						private final byte[] sigBytes = m.getSignature().getBytes();;

						@Override
						public byte[] getValue() throws AttributeValueException {
							return sigBytes;
						}
						
						@Override
						public String getName() {
							return "HostMethod";
						}
					};
					for(final Unit u : body.getUnits()) {
						final Stmt s = (Stmt) u;
						s.addTag(hostTag);
						this.runQueryTree(s, qt.getStmtExists(), this.stmtQuery, cm.stmtFoundFlag());
						this.runQueryTree(s, qt.getStmtSums(), this.stmtQuery, cm.stmtSumIncrementer());
						if(qt.hasExprQueries()) {
							for(final ValueBox vb : s.getUseBoxes()) {
								vb.addTag(hostTag);
								this.runQueryTree(vb, qt.getExprExists(), this.exprQuery, cm.exprFoundFlag());
								this.runQueryTree(vb, qt.getExprSums(), this.exprQuery, cm.exprSumIncrementer());
							}
						}
					}
				} catch(final OutOfMemoryError e) {
					context.getCounter(Diagnostics.OOM_METHOD).increment(1L);
					recordFailedMethod(context);
					return;
				} catch(final Exception e) {
					recordFailedMethod(context);
					return;
				}
			}
		} catch(final Exception e) {
			context.getCounter(Diagnostics.FAILED_CLASSES).increment(1L);
			writeQueryIncrement("$FAILED_CLASSES", context);
			return;
		}
		for(final String foundPattern : cm.foundExpr) {
			writeQueryIncrement(foundPattern, context);
		}
		for(final String foundPattern : cm.foundStmts) {
			writeQueryIncrement(foundPattern, context);
		}
		for(final Map.Entry<String, Integer> kv : cm.exprCounts.entrySet()) {
			writeQueryIncrement(kv.getKey(), kv.getValue(), context);
		}
		for(final Map.Entry<String, Integer> kv : cm.stmtCounts.entrySet()) {
			writeQueryIncrement(kv.getKey(), kv.getValue(), context);
		}
		writeQueryIncrement("$NUM_METHODS", context);
	}

	private void writeQueryIncrement(final String key, final int value, final Context context) throws IOException, InterruptedException {
		outputValue.set(key);
		count.set(value);
		context.write(outputValue, count);
	}

	private void recordFailedMethod(final Context context) throws IOException, InterruptedException {
		context.getCounter(Diagnostics.FAILED_METHODS).increment(1L);
		writeQueryIncrement(FAILED_METHOD_KEY, context);
	}
	
	private <T, M> void runQueryTree(final T val, final QueryGroup<T, M> qg, final F2<String, T, Boolean> interp, final MatchEffect<T, M> me) {
		for(final String root : qg.roots()) {
			this.runQueryTree(val, root, qg, interp, me);
		}
	}
	
	private <T, M> void runQueryTree(final T val, final String q, final QueryGroup<T, M> qg, final F2<String, T, Boolean> interp, final MatchEffect<T, M> me) {
		if(interp.f(q, val)) {
			me.f(q);
			for(final String subQuery : qg.children(q)) {
				this.runQueryTree(val, subQuery, qg, interp, me);
			}
		}
	}
	
	private void writeQueryIncrement(final String query, final Context context) throws IOException, InterruptedException {
		outputValue.set(query);
		context.write(outputValue, ONE_VALUE);
	}
}
