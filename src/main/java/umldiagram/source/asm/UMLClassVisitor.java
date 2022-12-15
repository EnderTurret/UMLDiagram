package umldiagram.source.asm;

import java.util.List;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import umldiagram.source.Access;
import umldiagram.source.asm.ASMUtil.Signature;
import umldiagram.source.data.FieldData;
import umldiagram.source.data.MethodData;
import umldiagram.util.Settings;

final class UMLClassVisitor extends ClassVisitor {

	private final List<FieldData> fields;
	private final List<MethodData> constructors;
	private final List<MethodData> methods;

	private String className;
	private String constructorName;

	private final Settings settings;

	public UMLClassVisitor(List<FieldData> fields, List<MethodData> constructors, List<MethodData> methods, Settings settings) {
		super(Opcodes.ASM9);
		this.fields = fields;
		this.constructors = constructors;
		this.methods = methods;
		this.settings = settings;
	}

	public String className() {
		return className;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		final Type type = Type.getObjectType(name);
		className = ASMUtil.simple(type.getClassName(), settings.useFqn());
		constructorName = ASMUtil.simple(type.getClassName(), false);

		if (signature != null) {
			final Signature sig = ASMUtil.parse(signature, className);
			if (sig.generic() != null)
				className += "<" + sig.generic() + ">";
		}
	}

	@Override
	public FieldVisitor visitField(int rawAccess, String name, String descriptor, String signature, Object value) {
		if ((rawAccess & Opcodes.ACC_SYNTHETIC) != 0) return null;

		final Access access = Access.forModifiers(rawAccess);
		if (settings.isVisible(access))
			fields.add(new FieldData(name, ASMUtil.parse(descriptor, signature, className).declaration()[0], access));

		return null;
	}

	@Override
	public MethodVisitor visitMethod(int rawAccess, String name, String descriptor, String signature, String[] exceptions) {
		if ((rawAccess & Opcodes.ACC_SYNTHETIC) != 0) return null;

		final Access access = Access.forModifiers(rawAccess);
		if (settings.isVisible(access) && !name.contains("lambda$") && !"<clinit>".equals(name)) {
			final MethodData data;
			final List<MethodData> add;

			final Signature sig = ASMUtil.parse(descriptor, settings.showGenerics() ? signature : null, className);

			if ("<init>".equals(name)) {
				data = new MethodData(constructorName, sig.returnType(), access, true, sig.toParameters());
				add = constructors;
			} else {
				data = new MethodData(name, sig.returnType(), access, false, sig.toParameters());
				add = methods;
			}

			return new ParameterCollector(data, add);
		}

		return null;
	}
}