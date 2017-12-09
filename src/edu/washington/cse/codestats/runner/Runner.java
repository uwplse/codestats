package edu.washington.cse.codestats.runner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import edu.washington.cse.codestats.CompiledQuery;
import edu.washington.cse.codestats.Compiler;
import edu.washington.cse.codestats.ParseException;
import edu.washington.cse.codestats.TokenMgrError;
import edu.washington.cse.codestats.hadoop.StatMapper;
import edu.washington.cse.codestats.hadoop.StatReducer;

public class Runner {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(final String args[]) throws IOException, ClassNotFoundException, InterruptedException, ParseException, TokenMgrError {
		final String queryName = args[0];
		String programText;
		final String rawQuery = args[1];
		programText = readQuery(rawQuery);
		final CompiledQuery q = Compiler.compile(programText);
		final Configuration conf = new Configuration();
		final FileSystem fs = FileSystem.get(conf);
		final Job j = Job.getInstance(conf, "code stats: " + queryName);
		for(final FileStatus stat : fs.listStatus(new Path("/user/jtoman/codestat-jars"))) {
			j.addArchiveToClassPath(stat.getPath());
		}
		j.setMapperClass(StatMapper.class);
		j.setReducerClass(StatReducer.class);
		j.setCombinerClass(StatReducer.class);
		final String jar = Runner.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		final File jarFile = new File(jar);
		j.setJar(jarFile.getAbsolutePath());
		j.setInputFormatClass(SequenceFileInputFormat.class);
		j.setOutputFormatClass(TextOutputFormat.class);
		j.setMapOutputKeyClass(Text.class);
		j.setMapOutputValueClass(LongWritable.class);
		SequenceFileInputFormat.setInputDirRecursive(j, true);
		SequenceFileInputFormat.addInputPaths(j, "/user/jtoman/inputs");
		final Path outputPath = new Path("/user/jtoman/output/" + queryName);
		fs.delete(outputPath, true);
		FileOutputFormat.setOutputPath(j, outputPath);
		j.setOutputKeyClass(Text.class);
		j.setOutputValueClass(LongWritable.class);
		q.configure(j);
		j.setNumReduceTasks(1);
		j.getConfiguration().set("mapred.child.java.opts", "-Xmx1324m");
		j.getConfiguration().set("mapreduce.map.memory.mb", "1500");
		final Path tmpJarPath = new Path("/tmp/" + DigestUtils.sha256Hex(q.getJarFile().getAbsolutePath()) + "." + System.currentTimeMillis() + ".jar");
		fs.copyFromLocalFile(false, true, new Path(q.getJarFile().getAbsolutePath()), tmpJarPath);
		j.addArchiveToClassPath(tmpJarPath);
		fs.deleteOnExit(tmpJarPath);
		j.submit();
		j.waitForCompletion(true);
	}

	public static String readQuery(final String rawQuery) throws IOException {
		String programText;
		if (rawQuery.equals("-")) {
			programText = StringUtils.join(IOUtils.readLines(System.in, Charset.defaultCharset()), "\n");
		} else {
			programText = rawQuery;
		}
		return programText;
	}
}
