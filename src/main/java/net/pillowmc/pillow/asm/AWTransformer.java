package net.pillowmc.pillow.asm;

import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.quiltmc.loader.impl.QuiltLoaderImpl;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import net.fabricmc.accesswidener.AccessWidener;
import net.fabricmc.accesswidener.AccessWidenerClassVisitor;

public class AWTransformer implements ITransformer<ClassNode> {
    private final AccessWidener aw;
    public AWTransformer(){
        aw=QuiltLoaderImpl.INSTANCE.getAccessWidener();
    }

    @Override
    public @NotNull ClassNode transform(ClassNode input, ITransformerVotingContext context) {
        ClassNode output=new ClassNode(QuiltLoaderImpl.ASM_VERSION);
        ClassVisitor visitor=AccessWidenerClassVisitor.createClassVisitor(QuiltLoaderImpl.ASM_VERSION, output,
            QuiltLoaderImpl.INSTANCE.getAccessWidener()
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
        return aw.getTargets().stream()
            .map(i->i.replace(".", "/"))
            .map(name->ITransformer.Target.targetPreClass(name.replace('/', '.')))
            .collect(Collectors.toSet());
    }
    
}
