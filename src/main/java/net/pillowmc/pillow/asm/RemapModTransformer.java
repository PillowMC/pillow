package net.pillowmc.pillow.asm;

import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
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
        if (input.invisibleAnnotations == null) return input;
        boolean isMixin = false;
        for (AnnotationNode node : input.invisibleAnnotations) {
            if (node.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")) {
                isMixin = true;
                break;
            }
        }
        if (!isMixin) return input;
        for (FieldNode node : input.fields) {
            if (node.visibleAnnotations == null) continue;
            for (AnnotationNode ann : node.visibleAnnotations) {
                if (ann.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;")) {
                    node.name = remapper.mapFieldName("", node.name, node.desc);
                    return input;
                }
            }
        }
        for (MethodNode node : input.methods) {
            if (node.visibleAnnotations == null) continue;
            for (AnnotationNode ann : node.visibleAnnotations) {
                if (ann.desc.equals("Lorg/spongepowered/asm/mixin/Overwrite;")) {
                    node.name = remapper.mapMethodName("", node.name, node.desc);
                    return input;
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
        return ModJarProcessor.classes.stream().map(Target::targetPreClass).collect(Collectors.toSet());
    }

}
