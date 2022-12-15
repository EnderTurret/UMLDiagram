package net.enderturret.umldiagram.source.data;

import java.util.Objects;

import net.enderturret.umldiagram.source.IDiagramFormatter;

/**
 * Stores some data about a method parameter.
 * @author EnderTurret
 */
public final class ParameterData implements Comparable<ParameterData> {

	private final String name;
	private final String type;

	public ParameterData(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public String name() {
		return name;
	}

	public String type() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ParameterData)) return false;
		final ParameterData o = (ParameterData) obj;
		return Objects.equals(name, o.name) && Objects.equals(type, o.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}

	@Override
	public int compareTo(ParameterData o) {
		if (o == null) return -1;

		if (name != null) {
			if (o.name == null) return -1;
			int temp = name.compareTo(o.name);
			if (temp != 0) return temp;
		}

		return type.compareTo(o.type);
	}

	@Override
	public String toString() {
		if (name == null) return type;
		return name + ": " + type;
	}

	public String toFormattedString(IDiagramFormatter f) {
		if (name == null) return f.formatType(type);
		return f.formatParameter(name) + ": " + f.formatType(type);
	}
}