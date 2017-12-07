package edu.washington.cse.codestats;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.mapreduce.Job;

import edu.washington.cse.codestats.hadoop.StatMapper;

public class CompiledQuery {
	private final File jarFile;
	private final Map<String, Set<String>> exprExists;
	private final Map<String, Set<String>> stmtExists;
	private final Map<String, Set<String>> exprSum;
	private final Map<String, Set<String>> stmtSum;
	public final String interpreterName;

	public CompiledQuery(final File jarFile, final Map<String, Set<String>> exprExists, final Map<String, Set<String>> stmtExists,
			final Map<String, Set<String>> exprSum, final Map<String, Set<String>> stmtSum, final String interpreterName) {
		this.jarFile = jarFile;
		this.exprExists = exprExists;
		this.stmtExists = stmtExists;
		this.exprSum = exprSum;
		this.stmtSum = stmtSum;
		this.interpreterName = interpreterName;
	}

	public File getJarFile() {
		return jarFile;
	}

	public void configure(final Job j) {
		j.getConfiguration().set(StatMapper.INTERPRETER_CLASS_NAME, interpreterName);
		
		j.getConfiguration().set(StatMapper.EXPR_EXISTS, serialize(exprExists));
		j.getConfiguration().set(StatMapper.EXPR_SUM, serialize(exprSum));
		
		j.getConfiguration().set(StatMapper.STMT_EXISTS, serialize(stmtExists));
		j.getConfiguration().set(StatMapper.STMT_SUM, serialize(stmtSum));
	}

	private String serialize(final Map<String, Set<String>> map) {
		if(map.isEmpty()) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		for(final Map.Entry<String, Set<String>> kv : map.entrySet()) {
			sb.append(kv.getKey()).append("=");
			for(final String subQuery : kv.getValue()) {
				sb.append(subQuery).append(",");
			}
			if(!kv.getValue().isEmpty()) {
				sb.setLength(sb.length() - 1);
			}
			sb.append(";");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

}
