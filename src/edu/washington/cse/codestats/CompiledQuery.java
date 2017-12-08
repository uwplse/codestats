package edu.washington.cse.codestats;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Job;

import edu.washington.cse.codestats.hadoop.StatMapper;

public class CompiledQuery {
	private final File jarFile;
	private final String interpreterName;
	private final QueryTree queryTree;
	
	private static final String QUERY_TREE = "codestats.query.tree";

	public CompiledQuery(final File jarFile, final QueryTree qt, final String interpreterName) {
		this.jarFile = jarFile;
		this.queryTree = qt;
		this.interpreterName = interpreterName;
	}

	public File getJarFile() {
		return jarFile;
	}

	public void configure(final Job j) throws IOException {
		j.getConfiguration().set(StatMapper.INTERPRETER_CLASS_NAME, getInterpreterName());
		
		serializeTree(j.getConfiguration(), queryTree);
	}

	private static void serializeTree(final Configuration configuration, final QueryTree queryTree) throws IOException {
		ByteArrayOutputStream baos;
		final DataOutputStream dos = new DataOutputStream(baos = new ByteArrayOutputStream());
		queryTree.write(dos);
		configuration.set(QUERY_TREE, Base64.encodeBase64String(baos.toByteArray()));
	}
	
	public static void readTree(final Configuration config, final QueryTree qt) throws IOException {
		final DataInputStream dis = new DataInputStream(new ByteArrayInputStream(Base64.decodeBase64(config.get(QUERY_TREE))));
		qt.readFields(dis);
	}

	public String getInterpreterName() {
		return interpreterName;
	}

	public QueryTree getQueryTree() {
		return queryTree;
	}
}
