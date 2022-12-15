# UMLDiagram

A program that generates [UML diagrams](https://en.wikipedia.org/wiki/Unified_Modeling_Language#Structure_diagrams) for any compiled Java class file.
This program can be used instead of designing UML diagrams by hand.

For example, this is a generated UML diagram for the `UMLDiagram` class:

```
┌───────────────────────────────────────────────────┐
│                    UMLDiagram                     │
├───────────────────────────────────────────────────┤
│ - lines: List<Line>                               │
│ - clazzName: Line                                 │
│ - minWidth: int                                   │
├───────────────────────────────────────────────────┤
│ ~ UMLDiagram(lines: List<Line>)                   │
│ + chain(parents: UMLDiagram[]): ChainedUMLDiagram │
│ + clazzName(): String                             │
│ + minWidth(): int                                 │
│ + toString(leftPadding: int, width: int): String  │
│ + toString(): String                              │
└───────────────────────────────────────────────────┘
```

You can also generate UML diagrams for classes in the standard library.
For example, here's the one for `Object`:

```
┌───────────────────────────┐
│          Object           │
├───────────────────────────┤
│ + Object()                │
│ # finalize(): void        │
│ + wait(long, int): void   │
│ + wait(long): void        │
│ + wait(): void            │
│ + equals(Object): boolean │
│ + toString(): String      │
│ + hashCode(): int         │
│ + getClass(): Class<?>    │
│ # clone(): Object         │
│ - registerNatives(): void │
│ + notify(): void          │
│ + notifyAll(): void       │
└───────────────────────────┘
```

Members in UML diagrams can also be sorted, like so:

```
┌───────────────────────────┐
│          Object           │
├───────────────────────────┤
│ + Object()                │
│ # clone(): Object         │
│ + equals(Object): boolean │
│ # finalize(): void        │
│ + getClass(): Class<?>    │
│ + hashCode(): int         │
│ + notify(): void          │
│ + notifyAll(): void       │
│ - registerNatives(): void │
│ + toString(): String      │
│ + wait(): void            │
│ + wait(long): void        │
│ + wait(long, int): void   │
└───────────────────────────┘
```

Or non-public members can be hidden:

```
┌───────────────────────────┐
│          Object           │
├───────────────────────────┤
│ + Object()                │
│ + equals(Object): boolean │
│ + getClass(): Class<?>    │
│ + hashCode(): int         │
│ + notify(): void          │
│ + notifyAll(): void       │
│ + toString(): String      │
│ + wait(): void            │
│ + wait(long): void        │
│ + wait(long, int): void   │
└───────────────────────────┘
```

UML diagrams can be generated from class files outside of jar files or class files loaded in the class path.

Lastly, the program can also generate inheritance hierarchies:

```
            ┌───────────────────────────┐
            │          Object           │
            ├───────────────────────────┤
            │ + Object()                │
            │ + wait(long, int): void   │
            │ + wait(long): void        │
            │ + wait(): void            │
            │ + equals(Object): boolean │
            │ + toString(): String      │
            │ + hashCode(): int         │
            │ + getClass(): Class<?>    │
            │ + notify(): void          │
            │ + notifyAll(): void       │
            └───────────────────────────┘
                         ⇧
┌───────────────────────────────────────────────────┐
│                    UMLDiagram                     │
├───────────────────────────────────────────────────┤
│ + chain(parents: UMLDiagram[]): ChainedUMLDiagram │
│ + clazzName(): String                             │
│ + minWidth(): int                                 │
│ + toString(leftPadding: int, width: int): String  │
│ + toString(): String                              │
└───────────────────────────────────────────────────┘
                         ⇧
┌──────────────────────────────────────────────────┐
│                ChainedUMLDiagram                 │
├──────────────────────────────────────────────────┤
│ + ChainedUMLDiagram(chained: UMLDiagram[])       │
│ + ChainedUMLDiagram(chained: List<UMLDiagram>)   │
│ + minWidth(): int                                │
│ + toString(leftPadding: int, width: int): String │
└──────────────────────────────────────────────────┘
```

## Usage

This program can be used in one of two ways: by [using the GUI](#using-the-gui), or by [using the CLI](#using-the-cli).

### Using the GUI

The GUI can be accessed simply by running the application.

When it appears, you can generate a UML diagram by drag-and-dropping a class file onto the window or by using one of the options in the `File > Open...` submenu.

There are a few ways to customize the generated UML diagram in the `Settings` menu.
Each of these options has a tooltip that describes what they do.

There are also different options for copying the generated diagram under the `File > Copy to Clipboard...` submenu.

### Using the CLI

The CLI can be accessed by passing `--no-gui` as an argument to the application.
This will cause the application to print all of the usage information.

UML diagrams can be generated by passing either the fully qualified name of a class or the location of the class on disk.

## How it works

This application generates UML diagrams by first collecting class structure information.
There are two strategies used for this: [via reflection](#via-reflection), and [via ASM](#via-asm).

### Via Reflection

The first strategy is by using reflection.
This only works for classes that are present in the JVM's class path, as we can't perform reflection on classes the JVM doesn't know about.

This works by using the standard `getDeclaredMethods()`, `getDeclaredFields()`, and `getDeclaredConstructors()` methods on the `Class` instance.
Synthetic methods, fields, and constructors are omitted (you don't want UML diagrams of those anyway).

All of these members get parsed into `MethodData` and `FieldData` instances which get passed into the UML diagram generator.

#### Hacks

Sometimes, classes aren't as well-formed as you might hope.
The JVM is pretty resilient, but that doesn't mean we don't have to deal with these.
This is a list of hacks and workarounds employed in the `ReflectionSource`:

##### Recursive generics

In the case of `<E extends Enum<E>>`, you can have recursive generics.
This generic in particular expands to `<E extends Enum<E extends Enum<E extends...>>>` and will expand until you hit a `StackOverflowError`.

These don't exactly come from malformed class files, as you can find them in classes like `Enum` or some implementations of `Comparable`.
Regardless, these are handled by simply not endlessly expanding recursive generics.
It'll stop expanding once it detects a loop.

##### Signature and descriptor differences

Before this one can be explained, you'll need to know the difference between a "signature", and a "descriptor."

Java has this concept known as "type erasure," where the generics of a parameter or variable get "erased."
In the case of parameters of methods, you may have encountered situations where Java complains that two methods have the same erasure.
This happens when two methods have the same *descriptor*, but different *signatures*.

The descriptor of a method is the erased form of the method; how Java actually sees the method.
For example, say we have the method `void test(List<String> list)`.
The descriptor of this method is actually `void test(List)` (in a more readable form; the bytecode descriptor is actually `test(Ljava/util/List;)V`).

Signatures are the original form of the method. They are only present when there is generic information that was erased.
When present, they contain information about all the parameters in the method (not just the generic ones).

There are sometimes differences between the length of the descriptor and the length of the signature.
This usually happens with constructors in classes that are either enums or inner classes (that is, non-static classes).

When a class is an enum, its constructor gets two additional parameters at the beginning.
These are the `name` and `ordinal` parameters, which are necessary for the corresponding `name()` and `ordinal()` methods.

When a class is an inner class, its constructor gets one additional parameter at the beginning.
This is the enclosing class instance, which is usually accessed by `<class name>.this`.

All of this is automatically generated by the Java compiler; you'll never actually see this information.
Unfortunately, they are *very* visible from the reflection side.
More specifically, signatures don't contain this added information.
Because of this, the difference in length needs to be accounted for.

##### Hidden fields and methods

Most Java developers don't know about this, but some methods and fields are completely hidden from reflection.
As far as reflection is concerned, these members simply don't exist.
As these members are hidden, UML diagrams will not contain them.

Here's a list of all the hidden members:

* `sun.reflect.ConstantPool.constantPoolOop`
* `sun.reflect.UnsafeStaticFieldAccessorImpl.base`
* `sun.reflect.Reflection.fieldFilterMap`, `sun.reflect.Reflection.methodFilterMap`
* `sun.misc.Unsafe.getUnsafe()`
* `java.lang.System.security`
* `java.lang.Class.classLoader`
* `java.lang.Throwable.backtrace`

### Via ASM

The second strategy is by reading the class's bytecode directly.
The is accomplished by using [ASM](https://asm.ow2.io), which is a library designed for reading/writing Java bytecode.
This works on both classes loaded by the JVM (most of the time) as well as other random class files.

Information that ASM parses is handled by a `ClassVisitor`, which is a sort of callback class that receives information about fields, methods, and other things as they're parsed.
This `ClassVisitor` can return "sub visitors" for information it's interested in, such as method bodies or field annotations.

Most of the information needed for generating UML diagrams can be obtained from the initial callback, with the exception of method parameter names.

Since the program is working with the bytecode, it has to deal with the internal representations of descriptors and signatures.
Fortunately, ASM provides a `TraceSignatureVisitor` which can convert these into more readable forms... mostly.

#### Hacks

No application is complete without a few thousand edge cases, and this application is no exception.
Here are some hacks that the `BytecodeSource` has to deal with.

##### `TraceSignatureVisitor` problems

There are actually a few different problems with `TraceSignatureVisitor`'s output, some of which are just differences in presentation from what we might want.

First, all of the classes in the descriptor/signature end up with fully qualified names.
This is pretty obviously not ideal for UML diagrams, so those need to be removed.

Second, `TraceSignatureVisitor` outputs a single string, so that has to be split into the parameters and generic information.

There are also some other issues:

* Sometimes the return type will just be `[]`, instead of `Object[]`.
* Sometimes array information will just get dropped from the parameter information.
* Sometimes the return type will not be read.
* Sometimes the declaration will have ` extends ` at the beginning.
* `TraceSignatureVisitor` likes to output `? extends Object`, when it can just be `?`.

##### Lambdas and static initializers

As someone who's looked at class internals might know, lambdas and static initializers are actually methods.
Static initializers have the name `<clinit>` (meaning class initializer) and lambdas have names in the form of `lambda$<method>$<identifier>`, such as `lambda$main$0` (some compilers give lambdas different names).

For obvious reasons, lambdas and initializers don't really belong in UML diagrams.
As such, they are excluded from the diagrams.

### Putting it all together

After the class member information is collected, it gets assembled into the final UML diagram.

First, the information is laid out into individual lines in a `List`.
At this point, the `List` will contain something like:

```
UMLDiagram
separator
 - lines: List<Line>
 - clazzName: Line
 - minWidth: int
separator
 ~ UMLDiagram(lines: List<Line>)
 + chain(parents: UMLDiagram[]): ChainedUMLDiagram
 + clazzName(): String
 + minWidth(): int
 + toString(leftPadding: int, width: int): String
 + toString(): String
```

Then, the max width is calculated and the diagram border is added. Additionally, the `separator` strings are replaced with lines.
Lastly, any other UML diagrams are chained together to form the final diagram layout.

## Building from source

Just run one of the following commands:

* On Linux/macOS: `./gradlew build`
* On Windows: `gradlew.bat build`

This will build the program and place it in `libs` inside the `build` directory.