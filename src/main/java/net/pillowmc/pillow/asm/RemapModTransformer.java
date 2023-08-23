package net.pillowmc.pillow.asm;

import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import net.pillowmc.pillow.ModJarProcessor;

public class RemapModTransformer implements ITransformer<ClassNode> {
    private final Remapper remapper;

    RemapModTransformer() {
        var mappings = QuiltLauncherBase.getLauncher().getMappingConfiguration().getMappings();
        remapper = RemapperUtils.create(mappings, PillowNamingContext.fromName, PillowNamingContext.toName);
    }

    @Override
    public @NotNull ClassNode transform(ClassNode input, ITransformerVotingContext context) {
        ClassNode output = new ClassNode();
        input.accept(new PillowClassRemapper(output, remapper));
        return output;
    }

    @Override
    public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    @Override
    public @NotNull Set<Target> targets() {
        return ModJarProcessor.classes.stream().map(Target::targetPreClass).collect(Collectors.toSet());
    }

}
