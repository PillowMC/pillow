package net.pillowmc.pillow.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.Remapper;

public class PillowClassRemapper extends ClassRemapper {

    private static final Handle META_FACTORY = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false);
    private static final Handle ALT_META_FACTORY = new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "altMetafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;", false);

    public PillowClassRemapper(ClassVisitor classVisitor, Remapper remapper) {
        super(classVisitor, remapper);
    }

    protected MethodVisitor createMethodRemapper(final MethodVisitor methodVisitor) {
        return new MethodRemapper(methodVisitor, remapper) {
            @Override
            public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                if (!isLambdaMeta(bootstrapMethodHandle)) {
                    super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
                    return;
                }
                var remappedBootstrapMethodArguments = new Object[bootstrapMethodArguments.length];
                for (int i=0;i<bootstrapMethodArguments.length;i++) {
                    remappedBootstrapMethodArguments[i] = remapper.mapValue(bootstrapMethodArguments[i]);
                }
                mv.visitInvokeDynamicInsn(
                    remapper.mapMethodName(Type.getReturnType(descriptor).getInternalName(), name, ((Type)bootstrapMethodArguments[0]).getDescriptor()),
                    remapper.mapMethodDesc(descriptor), (Handle) remapper.mapValue(bootstrapMethodHandle), remappedBootstrapMethodArguments);
            }

            private boolean isLambdaMeta(Handle bootstrapMethodHandle) {
                return META_FACTORY.equals(bootstrapMethodHandle) || ALT_META_FACTORY.equals(bootstrapMethodHandle);
            }
        };
    }
}