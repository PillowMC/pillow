package net.pillowmc.pillow.asm.qsl.recipe;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.MethodNode;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import net.pillowmc.pillow.asm.PillowNamingContext;

public class RecipeManagerMixinTransformer implements ITransformer<MethodNode> {

    @Override
    public @NotNull MethodNode transform(MethodNode input, ITransformerVotingContext context) {
        if(!PillowNamingContext.isUserDev)input.name="m_44032_";
        return input;
    }

    @Override
    public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    @Override
    public @NotNull Set<Target> targets() {
        return Set.of(Target.targetMethod("org.quiltmc.qsl.recipe.mixin.RecipeManagerMixin", "lambda$apply$1", "(Ljava/util/Map$Entry;)Ljava/util/Map;"));
    }
    
}
