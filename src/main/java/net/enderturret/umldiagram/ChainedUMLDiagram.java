package net.enderturret.umldiagram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.enderturret.umldiagram.util.Util;

/**
 * Represents a chain of {@link UMLDiagram UMLDiagrams}.
 * @author EnderTurret
 * @see UMLDiagram#chain(UMLDiagram...)
 */
public class ChainedUMLDiagram extends UMLDiagram {

	private final List<UMLDiagram> chained;

	public ChainedUMLDiagram(UMLDiagram... chained) {
		super(Collections.emptyList());
		if (chained.length == 0) throw new IllegalArgumentException("Must have at least one UMLDiagram");
		this.chained = new ArrayList<>();
		Collections.addAll(this.chained, chained);
	}

	public ChainedUMLDiagram(List<UMLDiagram> chained) {
		super(Collections.emptyList());
		if (chained.isEmpty()) throw new IllegalArgumentException("Must have at least one UMLDiagram");
		this.chained = new ArrayList<>(chained);
	}

	@Override
	public int minWidth() {
		return chained.stream().mapToInt(UMLDiagram::minWidth).max().getAsInt();
	}

	@Override
	public String toString(int leftPadding, int width) {
		width = Math.max(width, minWidth());
		final int arrowPos = width / 2;

		final StringBuilder sb = new StringBuilder();

		for (UMLDiagram uml : chained) {
			if (sb.length() != 0)
				sb.append("\n").append(Util.repeat(" ", arrowPos)).append("⇧").append("\n");

			final int leftPad = (width - uml.minWidth()) / 2;

			sb.append(uml.toString(leftPad, uml.minWidth()));
		}

		return sb.toString();
	}
}