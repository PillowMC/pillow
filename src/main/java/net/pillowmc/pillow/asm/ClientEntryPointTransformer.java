package net.pillowmc.pillow.asm;

import java.util.ListIterator;
import java.util.Set;

import org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.quiltmc.loader.impl.FormattedException;
import org.quiltmc.loader.impl.entrypoint.EntrypointUtils;
import org.quiltmc.loader.impl.game.minecraft.Hooks;

public class ClientEntryPointTransformer implements ITransformer<MethodNode> {

    @SuppressWarnings("unused")
    public static void preLaunch(){
        try {
            EntrypointUtils.invoke("preLaunch", PreLaunchEntrypoint.class, PreLaunchEntrypoint::onPreLaunch);
        } catch (RuntimeException e) {
            throw new FormattedException("A mod crashed on startup!", e);
        }
    }

    @Override
    public @NotNull MethodNode transform(MethodNode input, ITransformerVotingContext context) {
        if(input.name.equals("main")){
            var newList=new InsnList();
            newList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, getClass().getName().replace(".", "/"), "preLaunch", "()V"));
            input.instructions.insertBefore(input.instructions.getFirst(), newList);
            return input;
        }
        // after initBackendSystem
        ListIterator<AbstractInsnNode> it=input.instructions.iterator();
        FieldInsnNode insn=null;
        while(it.hasNext()){
            AbstractInsnNode ins=it.next();
            if(ins instanceof FieldInsnNode fin){
                if((fin.desc.equals("Ljava/io/File;")&&fin.getOpcode()==Opcodes.PUTFIELD)){
                    insn=fin;
                    break;
                }
            }
        }
        if(insn==null)throw new RuntimeException("net.minecraft.client.Minecraft.<init> doesn't set gameDirectory!");
        it.add(new VarInsnNode(Opcodes.ALOAD, 0));
        it.add(new FieldInsnNode(Opcodes.GETFIELD, insn.owner, insn.name, insn.desc));
        it.add(new VarInsnNode(Opcodes.ALOAD, 0));
        it.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Hooks.INTERNAL_NAME, "startClient", "(Ljava/io/File;Ljava/lang/Object;)V"));
        return input;
    }

    @Override
    public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    @Override
    public @NotNull Set<Target> targets() {
        return Set.of(Target.targetMethod("net.minecraft.client.Minecraft", "<init>", "(Lnet/minecraft/client/main/GameConfig;)V"),
                Target.targetMethod("net.minecraft.client.main.Main", "main", "([Ljava/lang/String)V"));
    }
}
