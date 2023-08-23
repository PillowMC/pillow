package net.pillowmc.pillow.asm;

import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.quiltmc.loader.impl.QuiltLoaderImpl;
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import net.fabricmc.accesswidener.AccessWidener;
import net.fabricmc.accesswidener.AccessWidenerClassVisitor;

public class AWTransformer implements ITransformer<ClassNode> {
    private final AccessWidener aw;
    private final Remapper remapperIn;
    private final Remapper remapperOut;
    AWTransformer(){
        aw=QuiltLoaderImpl.INSTANCE.getAccessWidener();
        var mappings = QuiltLauncherBase.getLauncher().getMappingConfiguration().getMappings();
        remapperOut = RemapperUtils.create(mappings, PillowNamingContext.fromName, PillowNamingContext.toName);
        remapperIn = RemapperUtils.create(mappings, PillowNamingContext.toName, PillowNamingContext.fromName);
    }

    @Override
    public @NotNull ClassNode transform(ClassNode input, ITransformerVotingContext context) {
        ClassNode output=new ClassNode(QuiltLoaderImpl.ASM_VERSION);
        ClassVisitor remapOut=new PillowClassRemapper(output, remapperOut);
        ClassVisitor visitor=AccessWidenerClassVisitor.createClassVisitor(QuiltLoaderImpl.ASM_VERSION, remapOut,
            QuiltLoaderImpl.INSTANCE.getAccessWidener()
        );
        
        ClassVisitor remapIn=new PillowClassRemapper(visitor, remapperIn);
        input.accept(remapIn);
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
            .map(remapperOut::map)
            .map(name->ITransformer.Target.targetPreClass(name.replace('/', '.')))
            .collect(Collectors.toSet());
    }
    
}
