package edu.washington.cse.codestats.runner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;

public class SequenceWriter {
	/* Expects the following arguments: input directory and output file.
	 *
	 * The input directory is recursively searched for Java class files.
	 * The output directory is assumed to be relative to the current cluster's
	 * HDFS.
	 */
	public static void main(final String[] args) throws IOException {
		final Configuration conf = new Configuration();
		try(final Writer w = SequenceFile.createWriter(conf, Writer.file(new Path(args[1])), Writer.keyClass(Text.class), Writer.valueClass(BytesWritable.class))) {
            for (String pathname : SequenceWriter.list(args[0])) {
                final Text t = new Text();
                final BytesWritable v = new BytesWritable();
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final byte[] classBytes = baos.toByteArray();
                t.set(pathname.replaceFirst("[.]class", ""));
                IOUtils.copy(new FileInputStream(new File(args[0] + File.pathSeparator + pathname)), baos);
                v.set(classBytes, 0, classBytes.length);
                w.append(t, v);
            }
		}
	}

	private static List<String> list(String directory)  {
        List<String> listing = new ArrayList<String>();
        SequenceWriter.listUnder(directory, "", listing);
        return listing;
    }

    private static void listUnder(String directory, String subdirectory, List<String> listing)  {
        for (String filename : new File(directory + File.separator + subdirectory).list()) {
            String pathname = directory + File.separator + subdirectory + File.pathSeparator + filename;
            File file = new File(pathname);
            if (file.isDirectory()) {
                SequenceWriter.listUnder(directory, subdirectory + File.pathSeparator + filename, listing);
            } else if (file.isFile() && pathname.endsWith(".class")) {
                listing.add(subdirectory + File.pathSeparator + filename);
            }
        }
    }
}
