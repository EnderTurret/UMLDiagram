package net.enderturret.umldiagram.gui;

import net.enderturret.umldiagram.source.Access;
import net.enderturret.umldiagram.source.IDiagramFormatter;

/**
 * The formatter used for the GUI.
 * @author EnderTurret
 */
class HtmlDiagramFormatter implements IDiagramFormatter {

	@Override
	public String formatAccess(Access access) {
		return escapeHTML(Character.toString(access.character()));
	}

	@Override
	public String formatType(String type) {
		return escapeHTML(type);
	}

	@Override
	public String formatClassName(String name) {
		return escapeHTML(name);
	}

	@Override
	public String formatMethodName(String name) {
		return escapeHTML(name);
	}

	@Override
	public String formatParameter(String name) {
		return escapeHTML(name);
	}

	@Override
	public String formatFieldName(String name) {
		return escapeHTML(name);
	}

	// https://stackoverflow.com/a/25228492
	private static String escapeHTML(String s) {
		final StringBuilder out = new StringBuilder(Math.max(16, s.length()));

		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (c > 127 || c == '"' || c == '\'' || c == '<' || c == '>' || c == '&') {
				out.append("&#").append((int) c).append(';');
			} else
				out.append(c);
		}

		return out.toString();
	}
}