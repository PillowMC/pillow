package net.pillowmc.pillow.asm.qsl.resourceloader;

import java.util.ArrayList;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.util.Annotations;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;

public class MinecraftClientMixinTransformer implements ITransformer<ClassNode> {
    private static final String DO_LOAD_LEVEL_METHOD = "doLoadLevel(" +
			"Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Function;" +
			"ZLnet/minecraft/client/Minecraft$ExperimentalDialogType;Z)V";

    @Override
    public @NotNull ClassNode transform(ClassNode input, ITransformerVotingContext context) {
        input.methods.forEach(i->{
            var ann=Annotations.getVisible(i, Inject.class);
            if(ann==null){
                ann=Annotations.getVisible(i, ModifyVariable.class);
                if(ann==null){
                    ann=Annotations.getVisible(i, ModifyArg.class);
                    if(ann==null)return;
                }
            }
            var method=Annotations.<ArrayList<String>>getValue(ann, "method").get(0);
            if(method.contains("startIntegratedServer")){
                if(ann.desc.contains("Inject")){
                    i.desc="(" +
                    "Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Function;" +
                    "ZLnet/minecraft/client/Minecraft$ExperimentalDialogType;ZLorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V";
                }
                Annotations.setValue(ann, "method", DO_LOAD_LEVEL_METHOD);
                i.parameters.add(new ParameterNode("p_205210_", 0));
                LabelNode start, end;
                if(i.instructions.getFirst() instanceof LabelNode v)start=v;
                else {
                    start=new LabelNode();
                    i.instructions.insertBefore(i.instructions.get(0), start);
                }
                if(i.instructions.getLast() instanceof LabelNode v)end=v;
                else {
                    end=new LabelNode();
                    i.instructions.add(end);
                }
                i.localVariables.add(new LocalVariableNode("p_205210_", "Z", null, start, end, i.localVariables.toArray().length));
            }else if(method.contains("m_aaltpyph")){
                Annotations.setValue(ann, "method", "lambda$new$2(Ljava/lang/String;ILjava/util/Optional;)V");
                i.desc="(Ljava/lang/String;ILjava/util/Optional;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V";
                i.parameters.add(1, new ParameterNode("a", 0));
                i.parameters.add(2, new ParameterNode("b", 0));
                LabelNode start, end;
                if(i.instructions.getFirst() instanceof LabelNode v)start=v;
                else {
                    start=new LabelNode();
                    i.instructions.insertBefore(i.instructions.get(0), start);
                }
                if(i.instructions.getLast() instanceof LabelNode v)end=v;
                else {
                    end=new LabelNode();
                    i.instructions.add(end);
                }
                i.localVariables.add(1, new LocalVariableNode("a", "Ljava/lang/String;", null, start, end, 1));
                i.localVariables.add(2, new LocalVariableNode("b", "I", null, start, end, 2));
                for (AbstractInsnNode insn : i.instructions) {
                    if (insn instanceof VarInsnNode vinsn && vinsn.var >= 1) {
                        vinsn.var = 3;
                    }
                }
            }else if(i.name.equals("onEndReloadResources")){
                Annotations.setValue(ann, "method", "lambda$reloadResourcePacks$18(Ljava/util/concurrent/CompletableFuture;Ljava/util/Optional;)V");
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
        return Set.of(Target.targetClass("org.quiltmc.qsl.resource.loader.mixin.client.MinecraftClientMixin"));
    }
    
}
