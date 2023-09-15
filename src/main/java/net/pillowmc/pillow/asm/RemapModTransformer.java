package net.pillowmc.pillow.asm;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
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
import org.spongepowered.asm.lib.AnnotationVisitor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IRemapper;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.struct.MemberInfo;

public class RemapModTransformer implements ITransformer<ClassNode> {
    private final IRemapper remapper;

    RemapModTransformer() {
        var mappings = QuiltLauncherBase.getLauncher().getMappingConfiguration().getMappings();
//        remapper = RemapperUtils.create(mappings, PillowNamingContext.fromName, PillowNamingContext.toName);
        remapper = MixinEnvironment.getDefaultEnvironment().getRemappers();
    }

    @Override
    public @NotNull ClassNode transform(ClassNode input, ITransformerVotingContext context) {
        if (input.invisibleAnnotations == null) return input;
        boolean isMixin = false;
        String cn = null;
        for (AnnotationNode node : input.invisibleAnnotations) {
            if (node.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")) {
                var v = node.values.get(node.values.indexOf("value") + 1);
                if (v instanceof List l) {
                    cn = ((Type)l.get(0)).getInternalName();
                } else {
                    v = node.values.get(node.values.indexOf("targets") + 1);
                    if (v instanceof List<?> l)
                        cn = (String) l.get(0);
                    else throw new AssertionError();
                }
                break;
            }
        }
        if (null == cn) return input;
        for (FieldNode node : input.fields) {
            if (node.visibleAnnotations == null) continue;
            for (AnnotationNode ann : node.visibleAnnotations) {
                if (ann.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;")) {
                    node.name = remapper.mapFieldName(cn, node.name, node.desc);
                }
            }
        }
        for (MethodNode node : input.methods) {
            if (node.visibleAnnotations == null) continue;
            for (AnnotationNode ann : node.visibleAnnotations) {
                if ("Lorg/spongepowered/asm/mixin/Overwrite;".equals(ann.desc)) {
                    node.name = remapper.mapMethodName(cn, node.name, node.desc);
                } else if (ann.desc.startsWith("Lorg/spongepowered/asm/mixin/injection")) {
                    var methods = (List<String>)ann.values.get(ann.values.indexOf("method") + 1);
                    for (int i = 0; i < methods.size(); i++) {
                        methods.set(i, cn + "." + methods.get(i));
                    }
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
