package net.pillowmc.pillow.asm;

import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import net.fabricmc.accesswidener.AccessWidenerClassVisitor;
import net.fabricmc.loader.impl.FabricLoaderImpl;

public class AWTransformer implements ITransformer<ClassNode> {

    @Override
    public @NotNull ClassNode transform(ClassNode input, ITransformerVotingContext context) {
        ClassNode output=new ClassNode(FabricLoaderImpl.ASM_VERSION);
        ClassVisitor visitor=AccessWidenerClassVisitor.createClassVisitor(FabricLoaderImpl.ASM_VERSION, output,
            FabricLoaderImpl.INSTANCE.getAccessWidener()
        );
        input.accept(visitor);
        return output;
    }

    @Override
    public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    @Override
    public @NotNull Set<Target> targets() {
        return FabricLoaderImpl.INSTANCE.getAccessWidener().getTargets().stream()
            .map((name)->ITransformer.Target.targetPreClass(name.replace(".","/")))
            .collect(Collectors.toSet());
    }
    
}
