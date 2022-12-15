package net.enderturret.umldiagram.util;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import net.enderturret.umldiagram.source.Access;
import net.enderturret.umldiagram.source.IDiagramFormatter;

/**
 * An assortment of settings to configure what exactly should be in a generated UML diagram and how it should look.
 * @author EnderTurret
 */
public final class Settings {

	private boolean useFqn = false;
	private Set<Access> visibility = EnumSet.of(Access.PUBLIC);
	private boolean showGenerics = true;
	private boolean truncateGenericClassName = false;
	private boolean sort = true;
	private boolean withSuper = false;
	private boolean includeObject = false;
	private boolean noGui = false;
	private IDiagramFormatter formatter = IDiagramFormatter.NO_FORMATTING;

	public Settings() {}

	/**
	 * @param value Whether to use the fully qualified name of the class for the diagram header.
	 * @return {@code this}.
	 */
	public Settings useFqn(boolean value) {
		useFqn = value;
		return this;
	}

	private Settings visibility0(Set<Access> value) {
		visibility = value;
		return this;
	}

	/**
	 * @param value A {@link Set} of {@link Access} defining what visibility members need to have to be displayed.
	 * @return {@code this}.
	 */
	public Settings visibility(Set<Access> value) {
		return visibility0(EnumSet.copyOf(value));
	}

	/**
	 * Short for {@link #visibility(Set)} with all {@link Access} values.
	 * @return {@code this}.
	 */
	public Settings setAllVisible() {
		return visibility0(EnumSet.allOf(Access.class));
	}

	/**
	 * Adds the given {@link Access} to the set of visibilities.
	 * @param value An {@link Access} to add.
	 * @return {@code this}.
	 * @see #visibility(Set)
	 */
	public Settings addVisibility(Access value) {
		visibility.add(value);
		return this;
	}

	/**
	 * Adds all the given {@link Access Accesses} to the set of visibilities.
	 * @param values All the {@link Access Accesses} to add.
	 * @return {@code this}.
	 * @see #visibility(Set)
	 */
	public Settings addVisibility(Access... values) {
		Collections.addAll(visibility, values);
		return this;
	}

	/**
	 * @param value Whether to show generic information on types. If {@code false}, {@link List Lists} and other generic classes will look like raw types.
	 * @return {@code this}.
	 */
	public Settings showGenerics(boolean value) {
		showGenerics = value;
		return this;
	}

	/**
	 * @param value Whether the generic type information on generic classes should be truncated.
	 * @return {@code this}.
	 */
	public Settings truncateGenericClassName(boolean value) {
		truncateGenericClassName = value;
		return this;
	}

	/**
	 * @param value Whether to sort all members. If {@code false}, members will usually be in encounter order, but could be in any arbitrary order.
	 * @return {@code this}.
	 */
	public Settings sort(boolean value) {
		sort = value;
		return this;
	}

	/**
	 * @param value Whether to include super-classes in the diagram.
	 * @return {@code this}.
	 */
	public Settings withSuper(boolean value) {
		withSuper = value;
		return this;
	}

	/**
	 * @param value Whether to include {@link Object} when generating inheritance diagrams.
	 * @return {@code this}.
	 */
	public Settings includeObject(boolean value) {
		includeObject = value;
		return this;
	}

	/**
	 * @param value Whether to prevent the GUI from showing.
	 * @return {@code this}.
	 */
	public Settings noGui(boolean value) {
		noGui = value;
		return this;
	}

	/**
	 * @param value The formatter to use for formatting parts of the diagram.
	 * @return {@code this}.
	 */
	public Settings formatter(IDiagramFormatter value) {
		formatter = value;
		return this;
	}

	public boolean useFqn() {
		return useFqn;
	}

	public boolean isVisible(Access access) {
		return visibility.contains(access);
	}

	public boolean showGenerics() {
		return showGenerics;
	}

	public boolean truncateGenericClassName() {
		return truncateGenericClassName;
	}

	public boolean sort() {
		return sort;
	}

	public boolean withSuper() {
		return withSuper;
	}

	public boolean includeObject() {
		return includeObject;
	}

	public boolean noGui() {
		return noGui;
	}

	public IDiagramFormatter formatter() {
		return formatter;
	}
}