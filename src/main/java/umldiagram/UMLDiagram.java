package umldiagram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import umldiagram.source.ISource;
import umldiagram.source.data.Line;
import umldiagram.util.Util;

/**
 * Represents a UML diagram.
 * These can be queried for limited information, or turned into diagrams using {@link #toString(int, int)}.
 * @author EnderTurret
 */
public class UMLDiagram {

	private final List<Line> lines;
	private final Line clazzName;
	private final int minWidth;

	UMLDiagram(List<Line> lines) {
		this.lines = new ArrayList<>(lines);
		minWidth = this.lines.stream().mapToInt(Line::width).max().orElse(0);
		clazzName = this.lines.isEmpty() ? null : this.lines.remove(0);
	}

	/**
	 * Chains the given {@link UMLDiagram UMLDiagrams} together, adding this diagram to the end.
	 * This is used to create rudimentary inheritance hierarchies.
	 * @param parents A list of parent diagrams to be chained.
	 * @return The chained UML diagram.
	 */
	public ChainedUMLDiagram chain(UMLDiagram... parents) {
		final List<UMLDiagram> list = new ArrayList<>();
		Collections.addAll(list, parents);
		list.add(this);
		return new ChainedUMLDiagram(list);
	}

	/**
	 * @return The class name.
	 */
	public String clazzName() {
		return clazzName.text();
	}

	/**
	 * @return The minimum width the diagram will take up.
	 */
	public int minWidth() {
		return minWidth;
	}

	/**
	 * Creates a string representation of this {@link UMLDiagram}.
	 * @param leftPadding The number of spaces to pad the left of the diagram with.
	 * @param width The width of the diagram. Will be corrected to at least the minimum width.
	 * @return The diagram.
	 */
	public String toString(int leftPadding, int width) {
		if (width < minWidth)
			width = minWidth;

		final StringBuilder sb = new StringBuilder();

		final String sep = Util.repeat("─", width);
		final String leftPad = Util.repeat(" ", leftPadding);

		/*
		 * ┌───────────────────────────┐
		 * │         Rectangle         │
		 * ├───────────────────────────┤
		 * │ - length: double          │
		 * │ - width: double           │
		 * ├───────────────────────────┤
		 * │ + Rectangle()             │
		 * │ + setWidth(double): void  │
		 * │ + getWidth(): double      │
		 * │ + getArea(): double       │
		 * └───────────────────────────┘
		 */

		sb.append(leftPad).append("┌").append(sep).append("┐\n");

		double clazzPadding = (width - clazzName.width()) / 2.0;
		if (clazzPadding < 0) {
			System.err.println("Class name is longer than max line width???");
			System.err.println("Class name: " + clazzName.text() + " (" + clazzName.width() + ")");
			System.err.println("Max width: " + width);
			clazzPadding = 0;
		}

		sb.append(leftPad).append("│").append(Util.repeat(" ", (int) clazzPadding)).append(clazzName.text()).append(Util.repeat(" ", (int) Math.ceil(clazzPadding))).append("│");

		for (Line line : lines) {
			sb.append("\n");
			if (line == ISource.SEPARATOR_LINE) // == is okay here.
				sb.append(leftPad).append("├").append(sep).append("┤");
			else
				sb.append(leftPad).append("│").append(line).append(Util.repeat(" ", width - line.width())).append("│");
		}

		sb.append("\n").append(leftPad).append("└").append(sep).append("┘");

		return sb.toString();
	}

	@Override
	public String toString() {
		return toString(0, minWidth());
	}
}