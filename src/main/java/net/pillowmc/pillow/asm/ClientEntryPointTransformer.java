/*
 * Copyright (C) 2025 PillowMC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pillowmc.pillow.asm;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TargetType;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import java.util.ListIterator;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.quiltmc.loader.impl.game.minecraft.Hooks;

public class ClientEntryPointTransformer implements ITransformer<MethodNode> {
	@Override
	public @NotNull MethodNode transform(MethodNode input, ITransformerVotingContext context) {
		// after initBackendSystem
		ListIterator<AbstractInsnNode> it = input.instructions.iterator();
		FieldInsnNode insn = null;
		while (it.hasNext()) {
			AbstractInsnNode ins = it.next();
			if (ins instanceof FieldInsnNode fin) {
				if ((fin.desc.equals("Ljava/io/File;") && fin.getOpcode() == Opcodes.PUTFIELD)) {
					insn = fin;
					break;
				}
			}
		}
		if (insn == null)
			throw new RuntimeException("net.minecraft.client.Minecraft.<init> doesn't set gameDirectory!");
		it.add(new VarInsnNode(Opcodes.ALOAD, 0));
		it.add(new FieldInsnNode(Opcodes.GETFIELD, insn.owner, insn.name, insn.desc));
		it.add(new VarInsnNode(Opcodes.ALOAD, 0));
		it.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Hooks.INTERNAL_NAME, "startClient",
				"(Ljava/io/File;Ljava/lang/Object;)V"));
		return input;
	}

	@Override
	public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
		return TransformerVoteResult.YES;
	}

	@Override
	public @NotNull Set<Target<MethodNode>> targets() {
		return Set.of(Target.targetMethod("net.minecraft.client.Minecraft", "<init>",
				"(Lnet/minecraft/client/main/GameConfig;)V"));
	}

	@Override
	public @NotNull TargetType<MethodNode> getTargetType() {
		return TargetType.METHOD;
	}
}
