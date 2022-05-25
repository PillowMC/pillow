package net.pillowmc.pillow;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;

public class PillowJavaAgent implements ClassFileTransformer {
    public static void premain(String args, Instrumentation instrumentation){
        System.out.println("Pillow JavaAgent");
        instrumentation.addTransformer(new PillowJavaAgent());
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if(className.equals("net/fabricmc/loader/impl/FabricLoaderImpl")) {
            ClassNode node=new ClassNode(Opcodes.ASM9);
            new ClassReader(classfileBuffer).accept(node, 0);
            MethodNode mn=node.methods.stream().filter((methodNode -> methodNode.name.equals("setup"))).findFirst().orElseThrow();
            InsnList lst=new InsnList();
            Arrays.stream(mn.instructions.toArray()).map((in)->{
                if(in instanceof LdcInsnNode ldcInsnNode){
                    if(ldcInsnNode.cst instanceof String cst){
                        if(cst.equals("mods")) {
                            cst = "fabricMods";
                            ldcInsnNode.cst=cst;
                            return ldcInsnNode;
                        }
                    }
                }
                return in;
            }).forEach(lst::add);
            mn.instructions=lst;
            ClassWriter writer=new ClassWriter(0);
            node.accept(writer);
            return writer.toByteArray();
        }
        return classfileBuffer;
    }
}
