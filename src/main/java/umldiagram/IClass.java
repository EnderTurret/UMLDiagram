package umldiagram;

import umldiagram.util.Settings;

/**
 * Represents a sort of "pre-processed" UML diagram.
 * @author EnderTurret
 */
public interface IClass {

	/**
	 * Generates a {@link UMLDiagram}.
	 * @param settings The settings to configure the generator with.
	 * @return The generated diagram.
	 */
	public UMLDiagram toUML(Settings settings);

	/**
	 * Generates a {@link UMLDiagram} using reflection.
	 * @author EnderTurret
	 */
	public static class Reflected implements IClass {

		private final Class<?> clazz;

		public Reflected(Class<?> clazz) {
			this.clazz = clazz;
		}

		@Override
		public UMLDiagram toUML(Settings settings) {
			return UMLDiagrams.generate(settings, clazz);
		}
	}

	/**
	 * Generates a {@link UMLDiagram} using ASM.
	 * @author EnderTurret
	 */
	public static class ASM implements IClass {

		private final byte[] code;

		public ASM(byte[] code) {
			this.code = code;
		}

		@Override
		public UMLDiagram toUML(Settings settings) {
			return UMLDiagrams.generate(settings, code);
		}
	}
}