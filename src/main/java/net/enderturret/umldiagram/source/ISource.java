package net.enderturret.umldiagram.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import net.enderturret.umldiagram.source.data.FieldData;
import net.enderturret.umldiagram.source.data.Line;
import net.enderturret.umldiagram.source.data.MethodData;
import net.enderturret.umldiagram.util.Settings;

/**
 * Defines a "source" of field and method data for populating UML diagrams.
 * @author EnderTurret
 *
 * @param <T> The type of data the source handles.
 */
public interface ISource<T> {

	/**
	 * Marks that a line should be a separator in the diagram.
	 */
	public static final String SEPARATOR = "separator";

	/**
	 * {@link Line} version of {@link #SEPARATOR}.
	 */
	public static final Line SEPARATOR_LINE = new Line(SEPARATOR);

	/**
	 * <p>Populates the lines of a UML diagram.</p>
	 * <p>
	 * The lines will contain something like the following:
	 * <pre>
	 * {@code
	 * "Object",
	 * "separator",
	 * " + wait(long, int): void "
	 * }</pre>
	 * </p>
	 * @param lines A list to fill with lines.
	 * @param source The source data to gather method and field information from.
	 * @param settings The settings.
	 */
	public default void generate(List<Line> lines, T source, Settings settings) {
		final List<FieldData> fields = new ArrayList<>(2);
		final List<MethodData> constructors = new ArrayList<>(1);
		final List<MethodData> methods = new ArrayList<>(3);

		final String className = generate(fields, constructors, methods, source, settings);

		final IDiagramFormatter f = settings.formatter();

		if (settings.sort()) {
			Collections.sort(fields);
			Collections.sort(constructors);
			Collections.sort(methods);
		}

		lines.add(new Line(f.formatClassName(className), className.length()));

		if (!fields.isEmpty())
			lines.add(SEPARATOR_LINE);

		for (FieldData field : fields)
			lines.add(new Line(" " + field.toFormattedString(f) + " ", field.toString().length() + 2));

		if (!constructors.isEmpty() || !methods.isEmpty())
			lines.add(SEPARATOR_LINE);

		for (MethodData ctor : constructors)
			lines.add(new Line(" " + ctor.toFormattedString(f) + " ", ctor.toString().length() + 2));

		for (MethodData m : methods)
			lines.add(new Line(" " + m.toFormattedString(f) + " ", m.toString().length() + 2));
	}

	/**
	 * Gathers data from the source and fills each list.
	 * @param fields The field list to fill.
	 * @param constructors The constructor list to fill.
	 * @param methods The method list to fill.
	 * @param source The source data.
	 * @param settings The settings.
	 * @return The class name.
	 */
	public String generate(List<FieldData> fields, List<MethodData> constructors, List<MethodData> methods, T source, Settings settings);
}