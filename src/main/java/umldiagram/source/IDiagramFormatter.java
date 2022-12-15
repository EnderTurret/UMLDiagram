package umldiagram.source;

/**
 * Allows formatting parts of UML diagrams, allowing you to add colors or other stylistic things.
 * @author EnderTurret
 */
public interface IDiagramFormatter {

	/**
	 * A no-op formatter.
	 */
	public static final IDiagramFormatter NO_FORMATTING = new IDiagramFormatter() {};

	public default String formatAccess(Access access) { return Character.toString(access.character()); }
	public default String formatType(String type) { return type; }

	public default String formatClassName(String name) { return name; }

	public default String formatMethodName(String name) { return name; }
	public default String formatParameter(String name) { return name; }

	public default String formatFieldName(String name) { return name; }
}