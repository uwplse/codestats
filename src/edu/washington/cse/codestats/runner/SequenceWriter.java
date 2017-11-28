package edu.washington.cse.codestats.runner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;

public class SequenceWriter {
	public static void main(final String[] args) throws IOException {
		final Configuration conf = new Configuration();
		final Text t = new Text();
		final BytesWritable v = new BytesWritable();
		try(final Writer w = SequenceFile.createWriter(conf, Writer.file(new Path(args[0])), Writer.keyClass(Text.class), Writer.valueClass(BytesWritable.class))) {
			t.set(args[1]);
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(new FileInputStream(new File(args[2])), baos);
			final byte[] classBytes = baos.toByteArray();
			v.set(classBytes, 0, classBytes.length);
			w.append(t, v);
		}
	}
}
