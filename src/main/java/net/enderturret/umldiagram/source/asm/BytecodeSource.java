package net.enderturret.umldiagram.source.asm;

import java.util.List;

import org.objectweb.asm.ClassReader;

import net.enderturret.umldiagram.source.ISource;
import net.enderturret.umldiagram.source.data.FieldData;
import net.enderturret.umldiagram.source.data.MethodData;
import net.enderturret.umldiagram.util.Settings;

/**
 * A UML diagram generator source that uses the bytecode of a class.
 * @author EnderTurret
 */
public enum BytecodeSource implements ISource<byte[]> {

	INSTANCE;

	@Override
	public String generate(List<FieldData> fields, List<MethodData> constructors, List<MethodData> methods, byte[] source, Settings settings) {
		final UMLClassVisitor visitor = new UMLClassVisitor(fields, constructors, methods, settings);
		new ClassReader(source).accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES);
		return visitor.className();
	}
}