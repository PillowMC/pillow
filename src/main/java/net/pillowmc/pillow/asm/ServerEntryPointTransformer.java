package net.pillowmc.pillow.asm;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.quiltmc.loader.impl.FormattedException;
import org.quiltmc.loader.impl.entrypoint.EntrypointUtils;
import org.quiltmc.loader.impl.game.minecraft.Hooks;

import java.util.ListIterator;
import java.util.Set;

public class ServerEntryPointTransformer implements ITransformer<MethodNode> {

    public static void preLaunch(){
        try {
            EntrypointUtils.invoke("preLaunch", PreLaunchEntrypoint.class, PreLaunchEntrypoint::onPreLaunch);
        } catch (RuntimeException e) {
            throw new FormattedException("A mod crashed on startup!", e);
        }
    }

    @Override
    public @NotNull MethodNode transform(MethodNode input, ITransformerVotingContext context) {
        var newList=new InsnList();
        newList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, getClass().getName().replace(".", "/"), "preLaunch", "()V"));
        input.instructions.insertBefore(input.instructions.getFirst(), newList);
        // before server.properties
        ListIterator<AbstractInsnNode> it=input.instructions.iterator();
        while(it.hasNext()){
            AbstractInsnNode ins=it.next();
            if(ins instanceof LdcInsnNode lin){
                if((lin.cst.equals("server.properties"))){
                    it.previous();
                    it.add(new InsnNode(Opcodes.ACONST_NULL));
                    it.add(new InsnNode(Opcodes.ACONST_NULL));
                    it.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Hooks.INTERNAL_NAME, "startServer", "(Ljava/io/File;Ljava/lang/Object;)V"));
                    it.next();
                }
            }else if(ins instanceof MethodInsnNode min){
                if(min.owner.equals("net/minecraft/server/dedicated/DedicatedServer")&&min.name.equals("<init>")){
                    it.add(new InsnNode(Opcodes.DUP));
                    it.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Hooks.INTERNAL_NAME, "setGameInstance", "(Ljava/lang/Object;)V"));
                }
            }
        }
        return input;
    }

    @Override
    public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    @Override
    public @NotNull Set<Target> targets() {
        return Set.of(Target.targetMethod("net.minecraft.server.Main", "main", "([Ljava/lang/String)V"));
    }
}
