package test;

import net.enderturret.umldiagram.UMLDiagrams;
import net.enderturret.umldiagram.source.Access;
import net.enderturret.umldiagram.util.Settings;

import util.ClassFinder;

/**
 * Tests to make sure the UML diagrams are generating correctly.
 * @author EnderTurret
 */
public final class BulkStandardLibraryClassTests {

	private static int passed = 0;
	private static int processed = 0;
	private static int total = 0;

	/**
	 * Didn't I just see one of these earlier?
	 * @param args Program arguments.
	 */
	public static void main(String[] args) {
		//test(Generics.class, true, true, true);
		//test(String.class, true, true, true);
		//test(TestEnum.class, true, true, false);

		ClassFinder.findClasses(BulkStandardLibraryClassTests::testClass);

		System.out.printf("%d/%d test%s passed (%d skipped).\n", passed, processed, processed == 1 ? "" : "s", total - processed);
	}

	private static boolean testClass(String str) {
		total++;

		// These classes have certain members hidden from reflection, so we can't generate proper UML diagrams for them.
		if ("sun.reflect.ConstantPool".equals(str) // constantPoolOop
				|| "sun.reflect.UnsafeStaticFieldAccessorImpl".equals(str) // base
				|| "sun.reflect.Reflection".equals(str) // fieldFilterMap; methodFilterMap
				|| "java.lang.System".equals(str) // security
				|| "java.lang.Class".equals(str) // classLoader
				|| "java.lang.Throwable".equals(str) // backtrace
				|| "sun.misc.Unsafe".equals(str)) // getUnsafe()
			return true;

		// This demonstrates a weird generic array bug in ASM's TraceSignatureVisitor, which I cannot easily fix.
		if (str.startsWith("com.sun.javafx.css.converters.") || str.equals("com.sun.javafx.css.parser.CSSParser")
				|| str.startsWith("com.sun.javafx.scene.layout.region"))
			return true;

		// This class's constructor signature appears to be malformed and causes the ReflectionSource to explode.
		// More specifically, the signature is missing both parameters following the DragSource.
		// Ironically, it looks like the signature doesn't even contain information for the part that's generic.
		if ("javafx.embed.swing.SwingDnD$1StubDragGestureRecognizer".equals(str))
			return true;

		final Class<?> clazz;

		try {
			clazz = Class.forName(str, false, BulkStandardLibraryClassTests.class.getClassLoader());
			clazz.getDeclaredFields(); // Populate fields to make sure there aren't any CNFEs or NCDFEs.
			clazz.getDeclaredMethods(); // Same here.
		} catch (ClassNotFoundException | NoClassDefFoundError e) {
			// It's probably fine.
			return true;
		} catch (Throwable e) {
			System.err.println("Failed to find class " + str + ":");
			e.printStackTrace();
			return true;
		}

		try {
			processed++;
			if (test(clazz, true, true, false))
				passed++;
		} catch (Throwable e) {
			System.err.println("Failed to generate UML diagram for class " + str + ":");
			e.printStackTrace();
		}

		return true;
	}

	static boolean test(Class<?> clazz, boolean anyAccess, boolean showGenerics, boolean useFile) {
		final Settings settings = new Settings();
		if (anyAccess) settings.setAllVisible();
		if (showGenerics) settings.showGenerics(true);

		final String refUml = UMLDiagrams.generate(settings, clazz).toString().trim();
		final String bcUml = UMLDiagrams.generate(settings, ClassUtil.readClassBytecode(clazz)).toString().trim();

		return compare(refUml, bcUml, clazz.getSimpleName(), settings, useFile);
	}

	private static boolean compare(String refSrc, String bcSrc, String targetName, Settings settings, boolean useFile) {
		final String filename = targetName + (settings.isVisible(Access.PRIVATE) ? "-any" : "") + (settings.showGenerics() ? "" : "-trunc");
		if (useFile) {
			// Remove last trailing newline.
			final String expected = ClassUtil.getExpectedResult(filename).trim();

			if (!refSrc.equals(expected)) {
				System.out.printf("Reflective test " + filename + " failed:"
						+ "\nExpected:\n%s"
						+ "\n\nResult:\n%s"
						+ "\n\n", expected, refSrc);
				return false;
			}
			if (!bcSrc.equals(expected)) {
				System.out.printf("Bytecode test " + filename + " failed:"
						+ "\nExpected:\n%s"
						+ "\n\nResult:\n%s"
						+ "\n\n", expected, bcSrc);
				return false;
			}
		}

		else if (!bcSrc.equals(refSrc)) {
			System.out.printf("Test " + filename + " failed:"
					+ "\nReflective:\n%s"
					+ "\n\nBytecode:\n%s"
					+ "\n\n", refSrc, bcSrc);
			return false;
		}

		return true;
	}
}