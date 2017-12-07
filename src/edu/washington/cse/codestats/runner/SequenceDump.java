package edu.washington.cse.codestats.runner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.Text;

public class SequenceDump {
	public static void main(final String[] args) throws IllegalArgumentException, IOException {
		final Reader sf = new SequenceFile.Reader(new Configuration(), SequenceFile.Reader.file(new Path(args[0])));
		final Text k = new Text();
		final BytesWritable bw = new BytesWritable();
		while(sf.next(k, bw)) {
			if(k.toString().equals(args[1])) {
				final File outFile = new File(args[2] + "/" + args[1].replace('.', '/') + ".class");
				outFile.getParentFile().mkdirs();
				try(FileOutputStream fos = new FileOutputStream(outFile)) {
					fos.write(bw.getBytes(), 0, bw.getLength());
					System.out.println("Done");
					return;
				}
			}
		}
		System.out.println("not found");
	}
}
