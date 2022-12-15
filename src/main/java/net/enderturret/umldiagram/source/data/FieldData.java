package net.enderturret.umldiagram.source.data;

import java.util.Objects;

import net.enderturret.umldiagram.source.Access;
import net.enderturret.umldiagram.source.IDiagramFormatter;

/**
 * Stores some data about a particular field.
 * @author EnderTurret
 */
public final class FieldData implements Comparable<FieldData> {

	private final String name;
	private final String type;
	private final Access access;

	public FieldData(String name, String type, Access access) {
		this.name = name;
		this.type = type;
		this.access = access;
	}

	public String name() {
		return name;
	}

	public String type() {
		return type;
	}

	public Access access() {
		return access;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof FieldData)) return false;
		final FieldData o = (FieldData) obj;

		return access == o.access && Objects.equals(name, o.name) && Objects.equals(type, o.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(access, name, type);
	}

	@Override
	public String toString() {
		return access.character() + " " + name + ": " + type;
	}

	public String toFormattedString(IDiagramFormatter f) {
		return f.formatAccess(access) + " " + f.formatFieldName(name) + ": " + f.formatType(type);
	}

	@Override
	public int compareTo(FieldData o) {
		if (o == null) return 1;
		return name.compareTo(o.name);
	}
}