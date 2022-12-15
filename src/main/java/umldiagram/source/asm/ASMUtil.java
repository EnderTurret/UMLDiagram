package umldiagram.source.asm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.util.TraceSignatureVisitor;

import umldiagram.UMLDiagrams;
import umldiagram.source.data.ParameterData;
import umldiagram.util.Pair;
import umldiagram.util.Util;

final class ASMUtil {

	static String simple(String className, boolean useFqn) {
		if (!useFqn) {
			int idx = className.lastIndexOf('$');
			if (idx != -1) {
				className = className.substring(idx + 1);
				// Local classes have a name like 1FontLazyValue. No idea why.
				final Matcher m = LOCAL_CLASS.matcher(className);
				if (m.matches())
					return m.group(1);
			} else {
				idx = className.lastIndexOf('.');
				if (idx != -1)
					className = className.substring(idx + 1);
			}
		}

		return className;
	}

	static String parse(Type type) {
		if (type.getSort() == Type.ARRAY)
			return parse(type.getElementType()) + Util.repeat("[]", type.getDimensions());
		return simple(type.getClassName(), false);
	}

	private static final Pattern QUALIFIED_NAME_PATTERN = Pattern.compile("\\w+\\.(\\w+\\$)*");

	// Stuff like 1FontLazyValue
	private static final Pattern LOCAL_CLASS = Pattern.compile("\\d+([a-zA-Z_][a-zA-Z0-9_]*)");

	// "MyList<E>.MyObject"   "Map<K, V>.Entry<K, V>"
	//  ^^^^^^^^^^             ^^^^^^^^^^
	private static final Pattern INSTANCE_INNER_CLASS = Pattern.compile("\\w+<[\\w, ]+>\\.");

	static String removeFqn(String desc) {
		return QUALIFIED_NAME_PATTERN.matcher(desc).replaceAll("");
	}

	static Signature parse(String descriptor, String signature, String parentClassName) {
		final Signature desc = new Signature(Type.getType(descriptor));

		if (signature != null) {
			Signature sig = parse(signature, parentClassName);

			// Hack for javac emitting unnecessary signatures for enums that do not include the ordinal or name parameters, along with inner classes.
			if (sig.declaration.length < desc.declaration.length) {
				final int diff = desc.declaration.length - sig.declaration.length;
				final String[] decl = new String[desc.declaration.length];
				for (int i = 0; i < desc.declaration.length; i++)
					if (i < diff)
						decl[i] = desc.declaration[i];
					else
						decl[i] = sig.declaration[i - diff];
				sig = new Signature(sig.generic, decl, sig.returnType);
			}

			// Hack for TraceSignatureVisitor dropping array information sometimes.
			for (int i = 0; i < desc.declaration.length; i++)
				if (desc.declaration[i].endsWith("[]") && !sig.declaration[i].endsWith("[]"))
					sig.declaration[i] += "[]";

			// Hack for TraceSignatureVisitor sometimes not reading the return type.
			if (sig.method && sig.returnType.isEmpty() && desc.method && !desc.returnType.isEmpty())
				return new Signature(sig.generic, sig.declaration, desc.returnType);

			return sig;
		}

		return desc;
	}

	static Signature parse(String signature, String parentClassName) {
		final TraceSignatureVisitor tsv = new TraceSignatureVisitor(Opcodes.ASM9);
		new SignatureReader(signature).accept(tsv);

		return new Signature(tsv, signature, parentClassName);
	}

	static String[] splitParams(String input) {
		final List<String> params = new ArrayList<>(2);

		int lastSplitEnd = 0;
		int genericDepth = 0;
		boolean seenComma = false;

		for (int i = 0; i < input.length(); i++) {
			final int cp = input.codePointAt(i);
			if (cp == '>' && genericDepth > 0)
				genericDepth--;
			else if (cp == '<')
				genericDepth++;
			else if (cp == ',' && genericDepth == 0)
				seenComma = true;
			else if (cp == ' ' && seenComma) {
				params.add(input.substring(lastSplitEnd, i - 1));
				lastSplitEnd = i + 1;
				seenComma = false;
			}
		}

		params.add(input.substring(lastSplitEnd, input.length()));

		return params.toArray(new String[0]);
	}

	public static class Signature {

		private final String generic;
		private final String[] declaration;
		private final String returnType;
		private final boolean method;

		private Signature(String generic, String[] declaration, String returnType) {
			this.generic = generic;
			this.declaration = declaration;
			this.returnType = returnType;
			method = returnType != null;
		}

		private Signature(TraceSignatureVisitor tsv, String signature, String parentClassName) {
			final Pair<String, String[]> declaration = cleanDeclaration(tsv.getDeclaration(), parentClassName);
			generic = declaration.left();
			this.declaration = declaration.right();

			String ret = tsv.getReturnType();
			if (ret != null) {
				ret = removeFqn(ret);
				// Sometimes instead of Object[] TraceSignatureVisitor will just output [].
				// I suspect this is normally to truncate <T extends Object> to just <T>, but it doesn't work in this context.
				if ("[]".equals(ret))
					ret = "Object[]";
				else
					ret = INSTANCE_INNER_CLASS.matcher(ret).replaceAll("");

				returnType = ret;
				method = true;
			} else {
				returnType = null;
				method = false;
			}
		}

		private Signature(Type type) {
			generic = "";
			method = type.getSort() == Type.METHOD;
			if (!method)
				declaration = new String[] { parse(type) };
			else
				declaration = Arrays.stream(type.getArgumentTypes()).map(ASMUtil::parse).toArray(String[]::new);

			returnType = method ? parse(type.getReturnType()) : null;
		}

		private static Pair<String,String[]> cleanDeclaration(String decl, String parentClassName) {
			decl = removeFqn(decl);
			// Sometimes TraceSignatureVisitor will just output this at the beginning.
			if (decl.startsWith(" extends "))
				decl = decl.substring(" extends ".length());

			String generic = null;

			// "public <A, B> void get(A a, B b);", "<T extends List<?>> implements Iterable<T>"
			//         ^^^^^^                        ^^^^^^^^^^^^^^^^^^^
			if (decl.startsWith("<")) {
				int idx = decl.indexOf("> ");
				if (idx == -1)
					idx = decl.indexOf(">(");

				if (idx == -1 && decl.endsWith(">"))
					idx = decl.length() - 1;
				else if (idx == -1) idx = decl.indexOf('>');

				generic = decl.substring(1, idx);
				decl = decl.substring(idx + 1);
			}

			// Try to replace List<E>.MyObject with just MyObject.
			decl = INSTANCE_INNER_CLASS.matcher(decl).replaceAll("");

			// TraceSignatureVisitor likes to be a bit literal at times.
			decl = decl.replace("? extends Object", "?");

			String[] declaration;

			// public <A, B> void get(A a, B b);
			//                       ^^^^^^^^^^
			if (decl.startsWith("(")) {
				decl = decl.substring(1, decl.length() - 1);
				declaration = decl.isEmpty() ? new String[0] : splitParams(decl);
			} else if (decl.isEmpty())
				declaration = new String[0];
			else declaration = new String[] { decl };

			return new Pair<>(generic, declaration);
		}

		public String generic() {
			return generic;
		}

		public String[] declaration() {
			return declaration;
		}

		public ParameterData[] toParameters() {
			return Arrays.stream(declaration)
					.map(p -> new ParameterData(null, p))
					.toArray(ParameterData[]::new);
		}

		public String returnType() {
			return returnType;
		}

		@Override
		public String toString() {
			return generic + "(" + String.join(", ", declaration) + "): " + returnType;
		}
	}
}