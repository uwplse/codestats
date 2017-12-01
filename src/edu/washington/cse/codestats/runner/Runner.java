package edu.washington.cse.codestats.runner;

import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.hadoop.conf.Configuration;
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
		final CompiledQuery q = Compiler.compile("hello: count expression e where { e.kind == 'InstanceInvoke'}" +
				"world: count expression e within hello where { e.args[*] is constant }");
		final Configuration conf = new Configuration();
		final FileSystem fs = FileSystem.get(conf);
		final Job j = Job.getInstance(conf, "code stats");
		j.setMapperClass(StatMapper.class);
		j.setReducerClass(StatReducer.class);
		j.setCombinerClass(StatReducer.class);
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
