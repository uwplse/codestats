package edu.washington.cse.codestats.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class StatReducer extends Reducer<Text, LongWritable, Text, LongWritable>{
	private final LongWritable result = new LongWritable();

	@Override
	protected void reduce(final Text key, final Iterable<LongWritable> values, final Context context) throws IOException, InterruptedException {
		long sum = 0L;
		for(final LongWritable l : values) {
			sum += l.get();
		}
		result.set(sum);
		context.write(key, result);
	}
}
