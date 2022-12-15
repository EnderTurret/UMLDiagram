package umldiagram.source.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import umldiagram.source.Access;
import umldiagram.source.IDiagramFormatter;
import umldiagram.source.ISource;
import umldiagram.source.data.FieldData;
import umldiagram.source.data.MethodData;
import umldiagram.source.data.ParameterData;
import umldiagram.util.Settings;

/**
 * A UML diagram generator source that uses reflection.
 * @author EnderTurret
 */
public enum ReflectionSource implements ISource<Class<?>> {

	INSTANCE;

	private static final Pattern ANONYMOUS_CLASS = Pattern.compile("\\$([0-9]+)$");

	@Override
	public String generate(List<FieldData> fields, List<MethodData> constructors, List<MethodData> methods, Class<?> source, Settings settings) {
		String ctorName = simpleClassName(source);

		final StringBuilder clazzName = new StringBuilder(settings.useFqn() ? source.getName() : ctorName);

		final TypeVariable<?>[] types = source.getTypeParameters();

		if (types.length > 0) {
			final String[] names = new String[types.length];

			clazzName.append("<");

			for (int i = 0; i < types.length; i++) {
				if (i != 0)
					clazzName.append(", ");
				names[i] = type(types[i], settings.showGenerics(), settings.truncateGenericClassName());

				for (int j = 0; j < i; j++)
					names[i] = names[i].replace(names[j], types[j].getName());

				clazzName.append(names[i]);
			}

			clazzName.append(">");
		}

		final boolean isEnum = source.isEnum();
		boolean isMember = source.isMemberClass() && !Modifier.isStatic(source.getModifiers());

		// An incredible hack for "com.sun.xml.internal.ws.client.AsyncResponseImpl$1CallbackFuture".
		if (!isMember) {
			final Class<?> enclosing = source.getEnclosingClass();
			if (enclosing != null) {
				final String name = source.getName().substring(enclosing.getName().length());
				if (name.length() >= 2 && name.startsWith("$")) {
					final int cp = name.codePointAt(1);
					if (cp >= '1' && cp <= '9') // Do you ever get the feeling that javac is gaslighting you?
						isMember = true;
				}
			}
		}

		for (Field field : source.getDeclaredFields()) {
			if (field.isSynthetic()) continue;
			final Access access = Access.forModifiers(field.getModifiers());
			if (settings.isVisible(access))
				fields.add(new FieldData(field.getName(), type(field.getGenericType(), settings.showGenerics(), true), access));
		}

		for (Constructor ctor : source.getDeclaredConstructors()) {
			if (ctor.isSynthetic()) continue; // Not sure if these exist, but might as well handle them anyway.
			final Access access = Access.forModifiers(ctor.getModifiers());
			if (settings.isVisible(access))
				constructors.add(new MethodData(ctorName, "void", access, true, sig(ctor.getParameters(), ctor.getGenericParameterTypes(), settings.showGenerics(), isEnum, isMember)));
		}

		for (Method m : source.getDeclaredMethods()) {
			if (m.isSynthetic()) continue;
			final Access access = Access.forModifiers(m.getModifiers());
			if (settings.isVisible(access))
				methods.add(new MethodData(m.getName(), type(m.getGenericReturnType(), settings.showGenerics(), true), access, false, sig(m.getParameters(), m.getGenericParameterTypes(), settings.showGenerics(), isEnum, isMember)));
		}

		return clazzName.toString();
	}

	/**
	 * @param clazz The class.
	 * @return The class's simple name.
	 */
	private static String simpleClassName(Class<?> clazz) {
		String name = clazz.getSimpleName();

		if (name.isEmpty()) {
			final Matcher m = ANONYMOUS_CLASS.matcher(clazz.getName());
			if (m.find())
				name = m.group(1);
			else
				System.err.println("Failed to match anon class: " + clazz.getName());
		}
		// Even though we're using getSimpleName(), some classes still have a $ in them, like sun.rmi.server.Activation$ActivationSystemImpl_Stub.
		else if (name.contains("$"))
			name = name.substring(name.indexOf('$') + 1);

		return name;
	}

	/**
	 * <p>Decodes the given {@link Type} into a nicer string representation.</p>
	 * <p>Use this method in preference to {@link Type#getTypeName()} because this one uses simple names instead of fully qualified ones, which can be very long and not UML diagram-friendly.</p>
	 * @param type The type to decode.
	 * @param decodeGenerics Whether to decode generic information on the type, if present.
	 * @param truncateGenerics Whether to truncate type variables, i.e &lt;T extends List&gt; would become &lt;T&gt;.
	 * @return A string representation of the type.
	 */
	private static String type(Type type, boolean decodeGenerics, boolean truncateGenerics) {
		return type(type, decodeGenerics, truncateGenerics, null);
	}

	/**
	 * <p>Decodes the given {@link Type} into a nicer string representation.</p>
	 * <p>Use this method in preference to {@link Type#getTypeName()} because this one uses simple names instead of fully qualified ones, which can be very long and not UML diagram-friendly.</p>
	 * @param type The type to decode.
	 * @param decodeGenerics Whether to decode generic information on the type, if present.
	 * @param truncateGenerics Whether to truncate type variables, i.e &lt;T extends List&gt; would become &lt;T&gt;.
	 * @param parent The parent type. This is used to help prevent NPEs.
	 * @return A string representation of the type.
	 */
	private static String type(Type type, boolean decodeGenerics, boolean truncateGenerics, Type parent) {
		if (type instanceof Class)
			return simpleClassName((Class<?>) type);

		if (type instanceof ParameterizedType) {
			final ParameterizedType genericType = (ParameterizedType) type;
			final StringBuilder sb = new StringBuilder();

			sb.append(type(genericType.getRawType(), decodeGenerics, truncateGenerics));

			if (decodeGenerics) {
				final Type[] args = genericType.getActualTypeArguments();
				if (args.length > 0) { // Instance inner classes may have zero type arguments.
					sb.append("<");

					for (int i = 0; i < args.length; i++) {
						if (i != 0) sb.append(", ");
						sb.append(type(args[i], decodeGenerics, truncateGenerics, genericType));
					}

					sb.append(">");
				}
			}

			return sb.toString();
		}

		if (type instanceof WildcardType) {
			final WildcardType wildType = (WildcardType) type;
			final StringBuilder sb = new StringBuilder();

			final Type[] bounds;

			if (wildType.getLowerBounds().length > 0) {
				sb.append("? super ");
				bounds = wildType.getLowerBounds();
			}
			else if (wildType.getUpperBounds().length > 0 && !wildType.getUpperBounds()[0].equals(Object.class)) {
				sb.append("? extends ");
				bounds = wildType.getUpperBounds();
			}
			else {
				sb.append("?");
				bounds = new Type[0];
			}

			for (int i = 0; i < bounds.length; i++) {
				if (i != 0)
					sb.append(" & ");
				sb.append(type(bounds[i], decodeGenerics, truncateGenerics, wildType));
			}

			return sb.toString();
		}

		if (type instanceof GenericArrayType)
			return type(((GenericArrayType) type).getGenericComponentType(), decodeGenerics, truncateGenerics, type) + "[]";

		if (type instanceof TypeVariable) {
			final TypeVariable<?> tv = (TypeVariable<?>) type;
			final StringBuilder sb = new StringBuilder(tv.getName());

			if (!truncateGenerics) {
				final Type[] args = tv.getBounds();

				// No need for <T extends Object>.
				if (args.length == 1 && args[0] == Object.class)
					return sb.toString();

				// Hack to prevent StackOverflowErrors when trying to build "<E extends Enum<E extends Enum<...>>".
				// This can also happen with "<K extends Comparable<K extends Comparable<...>>".
				if (args.length == 1 && args[0] == parent)
					return sb.toString();

				sb.append(" extends ");

				for (int i = 0; i < args.length; i++) {
					if (i != 0) sb.append(", ");
					sb.append(type(args[i], decodeGenerics, truncateGenerics, tv));
				}
			}

			return sb.toString();
		}

		return type == null ? "null" : type.getTypeName();
	}

	/**
	 * <p>Decodes the given parameters into a {@link ParameterData} array.</p>
	 * <p>If parameter names are available, they are added to the returned data, otherwise only types will be used.</p>
	 * @param params The parameters of the method or constructor.
	 * @param genericParams The generic parameters of the method or constructor.
	 * @param decodeGenerics Whether to decode generics. See {@link #type(Type, boolean, boolean)}.
	 * @param isEnum {@code true} if the class containing the method or constructor is an {@link Enum}.
	 * @param isMember {@code true} if the class containing the method or constructor is an inner class.
	 * @return A string representation of the given method or constructor signature.
	 */
	private static ParameterData[] sig(Parameter[] params, Type[] genericParams, boolean decodeGenerics, boolean isEnum, boolean isMember) {
		final ParameterData[] ret = new ParameterData[params.length];
		boolean hasGenericParams = genericParams != null && genericParams.length > 0;

		for (int i = 0; i < params.length; i++) {
			final Parameter param = params[i];

			Type gParam;

			if (hasGenericParams) {
				int gIdx = i;

				if (genericParams.length != params.length) {
					if (isEnum)
						gIdx -= 2;
					else if (isMember) // Enums cannot also be members; they're always static.
						gIdx -= 1;
				}

				try {
					gParam = gIdx >= 0 ? genericParams[gIdx] : null;
				} catch (ArrayIndexOutOfBoundsException e) {
					gParam = null;
					hasGenericParams = false; // Just turn it off.
					System.err.println("Exception converting parameter to generic!\n"
							+ "(Attempting to map: " + param
							+ ";\nisEnum: " + isEnum
							+ "; isMember: " + isMember
							+ ";\nParams: "
							+ Arrays.stream(params).map(Object::toString).collect(Collectors.joining(",\n  ", "[\n  ", "\n]"))
							+ "; Generic Params: "
							+ Arrays.stream(genericParams).map(Object::toString).collect(Collectors.joining(",\n  ", "[\n  ", "\n]"))
							+ ")");
					e.printStackTrace();
				}
			} else
				gParam = null;

			final String name = param.isNamePresent() ? param.getName() : null;
			final String type = type(gParam != null ? gParam : param.getParameterizedType(), decodeGenerics, true);

			ret[i] = new ParameterData(name, type);
		}

		return ret;
	}
}