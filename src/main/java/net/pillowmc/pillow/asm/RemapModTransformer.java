package net.pillowmc.pillow.asm;

import java.util.Set;
import java.util.stream.Collectors;

import net.fabricmc.mapping.util.AsmRemapperFactory;
import net.pillowmc.pillow.ModJarProcessor;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;

public class RemapModTransformer implements ITransformer<ClassNode> {
    private final Remapper remapper;
    RemapModTransformer(){
        var mappings= QuiltLauncherBase.getLauncher().getMappingConfiguration().getMappings();
        var factory=new AsmRemapperFactory(mappings);
        remapper=factory.getRemapper("intermediary", "srg");
    }

    @Override
    public @NotNull ClassNode transform(ClassNode input, ITransformerVotingContext context) {
        ClassNode output=new ClassNode();
        input.accept(new ClassRemapper(output, remapper));
        return output;
    }

    @Override
    public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    @Override
    public @NotNull Set<Target> targets() {
        return ModJarProcessor.classes.stream().map(Target::targetClass).collect(Collectors.toSet());
    }
    
}
