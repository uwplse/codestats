package edu.washington.cse.codestats.runner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.io.IOUtils;

import edu.washington.cse.codestats.CompiledQuery;
import edu.washington.cse.codestats.Compiler;
import edu.washington.cse.codestats.ParseException;
import edu.washington.cse.codestats.QueryInterpreter;
import edu.washington.cse.codestats.TokenMgrError;
import edu.washington.cse.codestats.hadoop.StatMapper;

public class LocalRunner {
	public static void main(final String[] args) throws FileNotFoundException, IOException, ParseException, TokenMgrError, 
		ClassNotFoundException, InstantiationException, IllegalAccessException, InterruptedException {
		final CompiledQuery q = Compiler.compile(args[0]);
		final ClassLoader cl = new URLClassLoader(new URL[]{q.getJarFile().toURI().toURL()});
		final QueryInterpreter qi = (QueryInterpreter) cl.loadClass(q.getInterpreterName()).newInstance();
		final StatMapper mp = new StatMapper();
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try(FileInputStream fis = new FileInputStream(new File(args[1]))) {
			IOUtils.copy(fis, baos);
		}
		mp.runLocally(q.getQueryTree(), qi, baos.toByteArray(), args[2]);
	}
}
