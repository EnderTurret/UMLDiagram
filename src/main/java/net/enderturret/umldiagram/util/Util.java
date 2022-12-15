package net.enderturret.umldiagram.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * A generic utility class. When you've seen one, you've seen them all.
 * @author EnderTurret
 */
public final class Util {

	private Util() { throw new AssertionError("No"); }

	/**
	 * Basically backported String#repeat.
	 * @param str The string to repeat.
	 * @param count The number of times to repeat it.
	 * @return {@code str} repeated {@code count} times.
	 */
	public static String repeat(String str, int count) {
		if (count < 0) throw new IllegalArgumentException("Attempted to repeat \"" + str + "\" " + count + " times!");
		if (count == 0) return "";

		final StringBuilder sb = new StringBuilder(str.length() * count);

		for (int i = 0; i < count; i++)
			sb.append(str);

		return sb.toString();
	}

	public static String getStackTrace(Throwable e) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); PrintStream ps = new PrintStream(baos)) {
			e.printStackTrace(ps);
			return baos.toString();
		} catch (IOException e1) { // This shouldn't actually be possible.
			throw new IllegalStateException(e1);
		}
	}
}