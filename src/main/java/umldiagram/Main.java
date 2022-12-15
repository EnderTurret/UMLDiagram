package umldiagram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import umldiagram.gui.Gui;
import umldiagram.source.Access;
import umldiagram.util.Pair;
import umldiagram.util.Settings;

/**
 * It's a main class alright.
 * @author EnderTurret
 */
final class Main {

	private static final String VERSION;

	static {
		String version;
		try (InputStream is = Main.class.getResourceAsStream("/version"); InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr)) {
			version = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			version = "unknown";
		}
		VERSION = "${version}".equals(version) ? "dev" : version;
	}

	private static Pair<List<IClass>, Settings> parseArgs(String[] args) {
		final Settings settings = new Settings().sort(false);

		int lastArg = 0;
		int skip = 0;

		args:
			for (int i = 0; i < args.length; i++) {
				while (skip != 0) {
					skip--;
					lastArg++;
					continue args;
				}

				final String arg = args[i];

				if ("--help".equals(arg)) {
					final String jar = "UMLDiagram-" + VERSION + ".jar";
					System.out.println("Usage: java -cp <folder containing classes>:. -jar " + jar + " [arguments...] <classes>");
					System.out.println("   Or: java -jar " + jar + " [arguments...] </path/to/class>.class");
					System.out.println();
					System.out.println("Examples: java -cp /path/to/MyRectangle:. -jar " + jar + " MyRectangle");
					System.out.println("          java -jar " + jar + " java.lang.String");
					System.out.println("          java -jar " + jar + " --show package,protected java.util.ArrayList");
					System.out.println("          java -jar " + jar + " --show all MyRectangle.class");
					System.out.println("          java -jar " + jar + " --show all Animal.class java.lang.String");
					System.out.println(
							  "\n--help     Show this text and exit"
							+ "\n--version  Show the program version and exit"
							+ "\n"
							+ "\n--show [private|protected|package|all]"
							+ "\n               Show members with the specified visibility"
							+ "\n--with-super"
							+ "\n               Prepend the superclass's UML diagram"
							+ "\n--include-object"
							+ "\n               Prepend Object's UML diagram"
							+ "\n--fqn          Display the class's fully qualified name in the UML header"
							+ "\n--no-generics  Truncate generics down to raw types"
							+ "\n--sort         Sort members"
							+ "\n--no-gui       Do not show the GUI");
					return null;
				}

				else if ("--version".equals(arg)) {
					System.out.println(VERSION);
					return null;
				}

				else if ("--fqn".equals(arg))
					settings.useFqn(true);

				else if ("--show".equals(arg)) {
					if (i + 1 == args.length) {
						System.out.println("--show: expected one argument.");
						return null;
					}

					final String next = args[i + 1];
					final String[] access = "all".equals(next) ? new String[] {"protected", "package_private", "private"} : next.split(",");

					for (String acc : access) {
						if ("package".equals(acc))
							acc = "package_private";

						try {
							settings.addVisibility(Access.valueOf(acc.toUpperCase(Locale.ENGLISH)));
						} catch (IllegalArgumentException e) {
							System.out.println("--show: unknown access " + acc + ", expected one of: public, protected, package_private (or package), private, or all.");
						}
					}

					skip++;
				}

				else if ("--no-generics".equals(arg))
					settings.showGenerics(false);
				else if ("--sort".equals(arg))
					settings.sort(true);
				else if ("--with-super".equals(arg))
					settings.withSuper(true);
				else if ("--include-object".equals(arg))
					settings.includeObject(true);
				else if ("--no-gui".equals(arg))
					settings.noGui(true);
				else {
					if (arg.startsWith("-"))
						System.out.println("Unrecognized argument: " + arg);
					break;
				}

				lastArg++;
			}

		final List<IClass> list = new ArrayList<>(1);

		for (int i = lastArg; i < args.length; i++) {
			final String clazzName = args[i];

			if (clazzName.endsWith(".class")) {
				final Path path = Paths.get(clazzName);
				if (!Files.exists(path))
					System.out.println("The file \"" + path + "\" could not be found.");
				else if (Files.isDirectory(path))
					System.out.println("The file \"" + path + "\" is a directory.");
				else
					try {
						list.add(new IClass.ASM(Files.readAllBytes(path)));
					} catch (IOException e) {
						e.printStackTrace();
					}
			}

			else
				try {
					final Class clazz = Class.forName(clazzName, false, Thread.currentThread().getContextClassLoader());
					list.add(new IClass.Reflected(clazz));
				} catch (ClassNotFoundException e) {
					System.out.println("The class \"" + clazzName + "\" could not be found.");
				}
		}

		return new Pair<>(list, settings);
	}

	public static void main(String[] args) {
		if (args.length == 1 && "--no-gui".equals(args[0]))
			args = new String[] { "--no-gui", "--help" };

		final Pair<List<IClass>, Settings> pair = parseArgs(args);
		if (pair == null) return;

		if (pair.right().noGui())
			for (IClass clazz : pair.left())
				System.out.println(clazz.toUML(pair.right()));
		else {
			final IClass initial = pair.left().isEmpty() ? null : pair.left().get(0);
			new Gui(VERSION, initial);
		}
	}
}