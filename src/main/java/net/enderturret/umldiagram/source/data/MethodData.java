package net.enderturret.umldiagram.source.data;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import net.enderturret.umldiagram.source.Access;
import net.enderturret.umldiagram.source.IDiagramFormatter;

/**
 * Stores some data about a particular method.
 * @author EnderTurret
 */
public final class MethodData implements Comparable<MethodData> {

	private final String name;
	private final String returnType;
	private final Access access;
	private final boolean constructor;
	private final ParameterData[] params;

	public MethodData(String name, String returnType, Access access, boolean constructor, ParameterData... params) {
		this.name = name;
		this.returnType = returnType;
		this.access = access;
		this.constructor = constructor;
		this.params = params;
	}

	public String name() {
		return name;
	}

	public String returnType() {
		return returnType;
	}

	public boolean constructor() {
		return constructor;
	}

	public Access access() {
		return access;
	}

	public ParameterData[] params() {
		return params;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof MethodData)) return false;
		final MethodData o = (MethodData) obj;

		return access == o.access && Objects.equals(name, o.name) && Objects.equals(returnType, o.returnType)
				&& Arrays.equals(params, o.params);
	}

	@Override
	public int hashCode() {
		return Objects.hash(access, name, returnType, params);
	}

	@Override
	public String toString() {
		return access.character() + " " + name + "(" + Arrays.stream(params).map(Object::toString).collect(Collectors.joining(", ")) + ")"
				+ (constructor ? "" : ": " + returnType);
	}

	public String toFormattedString(IDiagramFormatter f) {
		return f.formatAccess(access) + " " + f.formatMethodName(name) + "("
				+ Arrays.stream(params).map(p -> p.toFormattedString(f)).collect(Collectors.joining(", "))
				+ ")" + (constructor ? "" : ": " + f.formatType(returnType));
	}

	@Override
	public int compareTo(MethodData o) {
		if (o == null) return 1;
		int temp = name.compareTo(o.name);
		if (temp != 0) return temp;

		for (int i = 0; i < Math.max(params.length, o.params.length); i++) {
			if (i >= params.length) return -1;
			if (i >= o.params.length) return 1;

			temp = params[i].compareTo(o.params[i]);
			if (temp != 0) return temp;
		}

		// These should be equal most of the time, with the exception of synthetic methods (which we're already filtering out).
		// There are some methods in J2DPrismGraphics that somehow aren't synthetic and yet are otherwise completely equal outside of visibility.
		temp = returnType.compareTo(o.returnType);
		if (temp != 0) return temp;

		return access.compareTo(o.access);
	}
}