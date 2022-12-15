package net.enderturret.umldiagram.source.asm;

import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import net.enderturret.umldiagram.source.IDiagramFormatter;
import net.enderturret.umldiagram.source.data.MethodData;
import net.enderturret.umldiagram.source.data.ParameterData;

final class ParameterCollector extends MethodVisitor {

	private final MethodData data;
	private final List<MethodData> add;

	private int idx = 0;

	ParameterCollector(MethodData data, List<MethodData> add) {
		super(Opcodes.ASM9);
		this.data = data;
		this.add = add;
	}

	@Override
	public void visitParameter(String name, int access) {
		if (name != null)
			data.params()[idx] = new ParameterData(name, data.params()[idx].type());
		idx++;
	}

	@Override
	public void visitEnd() {
		add.add(data);
	}
}