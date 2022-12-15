package umldiagram.source.data;

/**
 * Represents a line of (possibly formatted) text.
 * @author EnderTurret
 */
public final class Line {

	private final String text;
	private final int width;

	public Line(String text, int width) {
		this.text = text;
		this.width = width;
	}

	public Line(String text) {
		this(text, text.length());
	}

	public String text() {
		return text;
	}

	public int width() {
		return width;
	}

	@Override
	public String toString() {
		return text;
	}
}