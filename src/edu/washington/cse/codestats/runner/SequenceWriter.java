package edu.washington.cse.codestats.runner;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.io.Text;

import soot.toolkits.scalar.Pair;

public class SequenceWriter {
	private static interface ClassIterator {
		boolean hasNext();
		public Pair<String, byte[]> next() throws IOException;
	}
	
	private static class DirectoryIterator implements ClassIterator {
		private final String rootDir;
		private final ArrayList<String> classPaths;
		private Iterator<String> it;
		private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		public DirectoryIterator(final String rootDir) throws IOException {
			this.rootDir = rootDir;
			this.classPaths = new ArrayList<>();
			final java.nio.file.Path root = new File(rootDir).toPath();
			Files.walkFileTree(root, new SimpleFileVisitor<java.nio.file.Path>() {
				@Override
				public FileVisitResult visitFile(final java.nio.file.Path path, final BasicFileAttributes attr) throws IOException {
					if(attr.isRegularFile() && path.toString().endsWith(".class")) {
						classPaths.add(root.relativize(path).toString());
					}
					return FileVisitResult.CONTINUE;
				}
			});
			this.it = classPaths.iterator();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public Pair<String, byte[]> next() throws IOException {
			baos.reset();
			final String s = it.next();
			final String className = s.replaceFirst("\\.class$", "").replace('/', '.');
			try(FileInputStream fis = new FileInputStream(new File(rootDir + s))) {
				IOUtils.copy(fis, baos);
			}
			return new Pair<String, byte[]>(className, baos.toByteArray());
		}
	}
	
	private static class JarFileIterator implements ClassIterator {

		private final JarFile jarFile;
		private final Enumeration<JarEntry> entries;
		private JarEntry nextEntry;
		private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		public JarFileIterator(final String jarPath) throws IOException {
			jarFile = new JarFile(new File(jarPath));
			this.entries = jarFile.entries();
			this.findNextEntry();
		}

		private void findNextEntry() {
			nextEntry = null;
			while(entries.hasMoreElements()) {
				final JarEntry nextElement = entries.nextElement();
				if(nextElement.getName().endsWith(".class")) {
					nextEntry = nextElement;
					return;
				}
			}
		}

		@Override
		public boolean hasNext() {
			return nextEntry != null;
		}

		@Override
		public Pair<String, byte[]> next() throws IOException {
			baos.reset();
			final String clsName = nextEntry.getName().replaceFirst("\\.class$", "").replace('/', '.');
			Pair<String, byte[]> toReturn;
			try(InputStream is = jarFile.getInputStream(nextEntry)) {
				IOUtils.copy(is, baos);
				toReturn = new Pair<String, byte[]>(clsName, baos.toByteArray());
			}
			this.findNextEntry();
			return toReturn;
		}
	}
	
	public static void main(final String[] args) throws IOException {		ClassIterator it;
		if(args[0].endsWith(".jar")) {
			it = new JarFileIterator(args[0]);
		} else {
			it = new DirectoryIterator(args[0]); 
		}
		final Configuration conf = new Configuration();
		final Text k = new Text();
		final BytesWritable v = new BytesWritable();
		try(final Writer w = SequenceFile.createWriter(conf, Writer.file(new Path(args[1])), Writer.keyClass(Text.class), Writer.valueClass(BytesWritable.class))) {
			while(it.hasNext()) {
				final Pair<String, byte[]> cls = it.next();
				k.set(cls.getO1());
				v.set(cls.getO2(), 0, cls.getO2().length);
				w.append(k, v);
			}
		}
	}
}
