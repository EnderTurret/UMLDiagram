package net.enderturret.umldiagram.source;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.objectweb.asm.Opcodes;

/**
 * Represents Java visibility modifiers and the corresponding characters in UML diagrams.
 * @author EnderTurret
 */
public enum Access {

	PUBLIC('+'),
	PROTECTED('#'),
	PACKAGE_PRIVATE('~'),
	PRIVATE('-');

	private final char character;

	private Access(char character) {
		this.character = character;
	}

	/**
	 * @return The character that represents this {@link Access} in UML diagrams.
	 */
	public char character() {
		return character;
	}

	/**
	 * @param modifiers Some modifiers from {@link Field#getModifiers()} or {@link Method#getModifiers()}.
	 * @return The {@link Access} corresponding to the visibility described by the modifiers.
	 */
	public static Access forModifiers(int modifiers) {
		if (Modifier.isPublic(modifiers))
			return PUBLIC;
		if (Modifier.isProtected(modifiers))
			return PROTECTED;
		if (Modifier.isPrivate(modifiers))
			return PRIVATE;

		return PACKAGE_PRIVATE;
	}
}