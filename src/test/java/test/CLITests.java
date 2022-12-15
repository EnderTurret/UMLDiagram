package test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.function.Predicate;

/**
 * Various tests for the CLI.
 * @author EnderTurret
 */
public class CLITests {

	private static final MethodHandle MAIN;

	static {
		try {
			final Class<?> clazz = Class.forName("umldiagram.Main");
			final Method main = clazz.getDeclaredMethod("main", String[].class);
			main.setAccessible(true);
			MAIN = MethodHandles.publicLookup().unreflect(main);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	// TODO: Make these actual tests, instead of whatever they are right now.
	public static void main(String[] args) {
		test(str -> str.startsWith("Usage: "), "--help");
		test(str -> { System.out.println(str); return true; }, "classes.Generics");
		test(str -> { System.out.println(str); return true; }, "classes.TestEnum");
		test(str -> { System.out.println(str); return true; }, "--show", "all", "--sort", "java.lang.String");
		test(str -> { System.out.println(str); return true; }, "--with-super", "umldiagram.ChainedUMLDiagram");
		test(str -> { System.out.println(str); return true; }, "--with-super", "--include-object", "umldiagram.ChainedUMLDiagram");
		test(str -> { System.out.println(str); return true; }, "--with-super", "--include-object", "classes.TestEnum");
	}

	private static void test(Predicate<String> test, String... args) {
		final String output = test(args);

		if (!test.test(output)) {
			System.out.println("Test failed!");
		}
	}

	private static String test(String... args) {
		final String[] realArgs = new String[args.length + 1];
		realArgs[0] = "--no-gui";
		System.arraycopy(args, 0, realArgs, 1, args.length);

		beginCaptureOut();

		try {
			MAIN.invokeExact(realArgs);
		} catch (Throwable e) {
			e.printStackTrace();
		}

		return endCaptureOut();
	}

	private static PrintStream captured;
	private static ByteArrayOutputStream stream;

	private static void beginCaptureOut() {
		if (captured != null) throw new IllegalStateException("Already capturing!");
		captured = System.out;
		System.setOut(new PrintStream(stream = new ByteArrayOutputStream(), true));
	}

	private static String endCaptureOut() {
		System.setOut(captured);
		final ByteArrayOutputStream baos = stream;

		captured = null;
		stream = null;

		return baos.toString();
	}
}