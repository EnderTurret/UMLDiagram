package umldiagram;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import umldiagram.source.Access;
import umldiagram.source.ISource;
import umldiagram.source.asm.BytecodeSource;
import umldiagram.source.data.Line;
import umldiagram.source.reflect.ReflectionSource;
import umldiagram.util.Settings;
import umldiagram.util.Util;

/**
 * Generates UML diagrams for any class either reflectively or through bytecode analysis.
 * @see #generate(Settings, Class)
 * @see #generate(Settings, byte[], byte[][])
 * @author EnderTurret
 */
public final class UMLDiagrams {

	private UMLDiagrams() { throw new AssertionError("No"); }

	/**
	 * Generates a UML diagram for the given class.
	 * @param clazz The class to generate a UML diagram for.
	 * @param settings Configuration for the diagram's appearance, along with what all should be shown in it.
	 * @return The UML diagram.
	 */
	public static UMLDiagram generate(Settings settings, Class<?> clazz) {
		return maybeChain(settings, generate(ReflectionSource.INSTANCE, clazz, settings), includeObject -> {
			final List<UMLDiagram> grams = new ArrayList<>();
			Class<?> sup = clazz;

			while ((sup = sup.getSuperclass()) != null) {
				if (!includeObject && sup == Object.class)
					break;
				grams.add(0, generate(ReflectionSource.INSTANCE, sup, settings));
			}

			return grams.toArray(new UMLDiagram[0]);
		});
	}

	/**
	 * Generates a UML diagram for the given class bytecode.
	 * @param clazzData The Java bytecode of the class to generate a UML diagram for.
	 * @param settings Configuration for the diagram's appearance, along with what all should be shown in it.
	 * @param superClazzData An array containing the superclass hierarchy of {@code clazzData}.
	 * @return The UML diagram.
	 */
	public static UMLDiagram generate(Settings settings, byte[] clazzData, byte[]... superClazzData) {
		return maybeChain(settings, generate(BytecodeSource.INSTANCE, clazzData, settings), includeObject -> {
			final List<UMLDiagram> grams = Arrays.stream(superClazzData)
					.map(scd -> generate(BytecodeSource.INSTANCE, scd, settings))
					.collect(Collectors.toList());

			if (!includeObject && ("Object".equals(grams.get(0).clazzName()) || "java.lang.Object".equals(grams.get(0).clazzName())))
				grams.remove(0);

			return grams.toArray(new UMLDiagram[0]);
		});
	}

	private static <T> UMLDiagram generate(ISource<T> handler, T source, Settings settings) {
		final List<Line> lines = new ArrayList<>();
		handler.generate(lines, source, settings);
		return new UMLDiagram(lines);
	}

	private static UMLDiagram maybeChain(Settings settings, UMLDiagram diagram, Function<Boolean, UMLDiagram[]> parentGetter) {
		if (settings.withSuper())
			return diagram.chain(parentGetter.apply(settings.includeObject()));
		return diagram;
	}
}