package util;

import java.io.File;
import java.util.Enumeration;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ClassFinder {

	public static void findClasses(Predicate<String> visitor) {
		final String classpath = System.getProperty("java.class.path");
		final String[] paths = classpath.split(File.pathSeparator);

		final String javaHome = System.getProperty("java.home");
		File file = new File(javaHome + File.separator + "lib");
		if (file.exists())
			findClasses(file, file, true, visitor);

		for (String path : paths) {
			file = new File(path);
			if (file.exists())
				findClasses(file, file, false, visitor);
		}
	}

	@SuppressWarnings("resource")
	private static boolean findClasses(File root, File file, boolean includeJars, Predicate<String> visitor) {
		if (file.isDirectory()) {
			for (File child : file.listFiles())
				if (!findClasses(root, child, includeJars, visitor))
					return false;
		} else if (file.getName().toLowerCase().endsWith(".jar") && includeJars) {
			JarFile jar = null;
			try {
				jar = new JarFile(file);
			} catch (final Exception ex) {}

			if (jar != null) {
				final Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					final JarEntry entry = entries.nextElement();
					final String name = entry.getName();
					final int extIndex = name.lastIndexOf(".class");
					if (extIndex > 0)
						if (!visitor.test(name.substring(0, extIndex).replace("/", ".")))
							return false;
				}
			}
		}
		else if (file.getName().toLowerCase().endsWith(".class"))
			if (!visitor.test(createClassName(root, file)))
				return false;

		return true;
	}

	private static String createClassName(File root, File file) {
		final StringBuilder sb = new StringBuilder();
		final String fileName = file.getName();
		sb.append(fileName.substring(0, fileName.lastIndexOf(".class")));
		file = file.getParentFile();
		while (file != null && !file.equals(root)) {
			sb.insert(0, '.').insert(0, file.getName());
			file = file.getParentFile();
		}
		return sb.toString();
	}
}