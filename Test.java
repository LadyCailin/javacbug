/**
 * javac NPE in Types.interfaceCandidates() — Minimal Reproducer
 * ================================================================
 *
 * javac crashes with a NullPointerException when:
 * 1. An interface method in a .class file has ACC_BRIDGE | ACC_ABSTRACT | ACC_SYNTHETIC flags
 * 2. A class implements that interface
 * 3. Overloaded static methods accept both the interface and the implementing class
 * 4. A call site requires overload resolution between those methods
 *
 * The crash occurs in Types$DescriptorFilter.test() when javac checks
 * isFunctionalInterface() during MostSpecificCheck. The interfaceCandidates()
 * method returns an empty list for the bridge+abstract method, and .head NPEs.
 *
 * Stack trace:
 *   java.lang.NullPointerException: Cannot invoke
 *     "com.sun.tools.javac.code.Symbol$MethodSymbol.flags()" because
 *     "com.sun.tools.javac.code.Types.interfaceCandidates(...).head" is null
 *   at jdk.compiler/com.sun.tools.javac.code.Types$DescriptorFilter.test(Types.java:1000)
 *   ...
 *   at jdk.compiler/com.sun.tools.javac.comp.Resolve.mostSpecific(...)
 *
 * Tested on: Eclipse Adoptium JDK 21.0.10+7
 *
 * How to reproduce:
 *   1. Compile the library: javac -d classes lib/I.java lib/Sub.java lib/Util.java
 *   2. Run PatchFlags to add ACC_BRIDGE|ACC_SYNTHETIC to I.typeof():
 *      javac -cp asm-9.7.1.jar PatchFlags.java
 *      java -cp asm-9.7.1.jar;. PatchFlags classes/I.class
 *   3. Compile the test against the patched classes: javac -cp classes Test.java
 *      -> NPE in compiler
 *
 * Note: The ACC_BRIDGE | ACC_ABSTRACT | ACC_SYNTHETIC combination on an interface
 * method is unusual but valid bytecode. It can be produced by annotation processors
 * or bytecode manipulation tools that mark deprecated interface methods as bridge
 * methods for binary compatibility purposes.
 */
public class Test {
    public void test() {
        Util.foo(Sub.INSTANCE, Sub.INSTANCE, null);
    }
}
