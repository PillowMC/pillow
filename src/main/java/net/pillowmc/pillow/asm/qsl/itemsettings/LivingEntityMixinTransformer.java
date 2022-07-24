package net.pillowmc.pillow.asm.qsl.itemsettings;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;
import org.objectweb.asm.tree.VarInsnNode;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;

public class LivingEntityMixinTransformer implements ITransformer<MethodNode> {

    @Override
    public @NotNull MethodNode transform(MethodNode input, ITransformerVotingContext context) {
        input.desc="(Lnet/minecraft/world/item/ItemStack;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;Lnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/Item;)V";
        input.parameters.add(input.parameters.size()-1, new ParameterNode("slot", 0));
        var first=(LabelNode)input.instructions.getFirst();
        var last=(LabelNode)input.instructions.getLast();
        input.localVariables.add(3, new LocalVariableNode("slot", "Lnet/minecraft/world/entity/EquipmentSlot;", null, first, last, 3));
        input.instructions.forEach(i->{
            if(i instanceof VarInsnNode j&&(j.getOpcode()==Opcodes.ALOAD||j.getOpcode()==Opcodes.ASTORE)&&j.var>=2){
                j.var++;
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
        return Set.of(Target.targetMethod("org.quiltmc.qsl.item.setting.mixin.LivingEntityMixin", "onGetPreferredEquipmentSlot", "(Lnet/minecraft/world/item/ItemStack;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;Lnet/minecraft/world/item/Item;)V"));
    }
    
}
