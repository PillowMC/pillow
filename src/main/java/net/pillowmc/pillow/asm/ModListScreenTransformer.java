package net.pillowmc.pillow.asm;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;

public class ModListScreenTransformer implements ITransformer<MethodNode> {

    @Override
    public @NotNull MethodNode transform(MethodNode input, ITransformerVotingContext context) {
        if(input.name.equals("m_5541_")){
            input.access=Opcodes.ACC_PUBLIC;
            return input;
        }
        input.instructions.forEach(i->{
            if(i instanceof MethodInsnNode min){
                if(min.name.equals("m_5542_")){
                    min.name="m_5541_";
                }
            }
        });
        return input;
    }

    @Override
    public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    @Override
    public @NotNull Set<Target> targets() {
        return Set.of(Target.targetMethod("net.minecraftforge.client.gui.ModListScreen", "lambda$updateCache$12", "(Lnet/minecraftforge/forgespi/language/IModInfo;Ljava/lang/String;)Lorg/apache/commons/lang3/tuple/Pair;"),
        Target.targetMethod("net.minecraftforge.resource.PathResourcePack", "m_5541_", "(Ljava/lang/String;)Ljava/io/InputStream;"));
    }
    
}
