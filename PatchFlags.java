import org.objectweb.asm.*;
import java.nio.file.*;

/**
 * Patches I.class to add ACC_BRIDGE | ACC_SYNTHETIC flags to the
 * deprecated typeof() method. This simulates what an annotation
 * processor or bytecode tool might do to create "aggressive deprecation"
 * (source-incompatible but binary-compatible deprecated methods).
 *
 * Usage: java -cp asm-9.7.1.jar;. PatchFlags classes/I.class
 */
public class PatchFlags extends ClassVisitor {
    public PatchFlags(ClassVisitor cv) {
        super(Opcodes.ASM9, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        if (name.equals("typeof") && descriptor.equals("()Ljava/lang/Object;")) {
            access |= Opcodes.ACC_BRIDGE | Opcodes.ACC_SYNTHETIC;
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    public static void main(String[] args) throws Exception {
        Path path = Paths.get(args[0]);
        ClassReader cr = new ClassReader(Files.readAllBytes(path));
        ClassWriter cw = new ClassWriter(0);
        cr.accept(new PatchFlags(cw), 0);
        Files.write(path, cw.toByteArray());
        System.out.println("Patched " + path);
    }
}
