package edu.washington.cse.codestats.runner;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import edu.washington.cse.codestats.CompiledQuery;
import edu.washington.cse.codestats.Compiler;
import edu.washington.cse.codestats.hadoop.StatMapper;
import edu.washington.cse.codestats.hadoop.StatReducer;

public class Runner {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(final String args[]) throws IOException, ClassNotFoundException, InterruptedException {
		final CompiledQuery q = Compiler.compile("swagtastic!");
		final ClassLoader queryClassLoader = new URLClassLoader(new URL[] { q.getJarFile().toURI().toURL() });
		final Configuration conf = new Configuration();
		final FileSystem fs = FileSystem.get(conf);
		final Job j = Job.getInstance(conf, "code stats");
		final Class<?> mapperClass = queryClassLoader.loadClass(StatMapper.class.getName());
		final Class<?> reducerClass = queryClassLoader.loadClass(StatReducer.class.getName());
		j.setMapperClass((Class<? extends Mapper>) mapperClass);
		j.setReducerClass((Class<? extends Reducer>) reducerClass);
		j.setCombinerClass((Class<? extends Reducer>) reducerClass);
		j.setJar(q.getJarFile().getAbsolutePath());
		j.setInputFormatClass(SequenceFileInputFormat.class);
		j.setOutputFormatClass(TextOutputFormat.class);
		j.setMapOutputKeyClass(Text.class);
		j.setMapOutputValueClass(LongWritable.class);
		SequenceFileInputFormat.addInputPaths(j, "/user/jtoman/test");
		FileOutputFormat.setOutputPath(j, new Path("/user/jtoman/output"));
		j.setOutputKeyClass(Text.class);
		j.setOutputValueClass(LongWritable.class);
		q.configure(j);
		final Path tmpJarPath = new Path("/tmp/" + DigestUtils.sha256Hex(q.getJarFile().getAbsolutePath()) + "." + System.currentTimeMillis() + ".jar");
		fs.copyFromLocalFile(false, true, new Path(q.getJarFile().getAbsolutePath()), tmpJarPath);
		j.addArchiveToClassPath(tmpJarPath);
		fs.deleteOnExit(tmpJarPath);
		j.submit();
		j.waitForCompletion(true);
	}
}
