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
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.quiltmc.loader.impl.game.minecraft.Hooks;

public class ServerEntryPointTransformer implements ITransformer<MethodNode> {
	@Override
	public @NotNull MethodNode transform(MethodNode input, ITransformerVotingContext context) {
		// before server.properties
		ListIterator<AbstractInsnNode> it = input.instructions.iterator();
		while (it.hasNext()) {
			AbstractInsnNode ins = it.next();
			if (ins instanceof LdcInsnNode lin) {
				if ((lin.cst.equals("server.properties"))) {
					it.previous();
					it.add(new InsnNode(Opcodes.ACONST_NULL));
					it.add(new InsnNode(Opcodes.ACONST_NULL));
					it.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Hooks.INTERNAL_NAME, "startServer",
							"(Ljava/io/File;Ljava/lang/Object;)V"));
					it.next();
				}
			} else if (ins instanceof MethodInsnNode min) {
				if (min.owner.equals("net/minecraft/server/dedicated/DedicatedServer") && min.name.equals("<init>")) {
					it.add(new InsnNode(Opcodes.DUP));
					it.add(new MethodInsnNode(Opcodes.INVOKESTATIC, Hooks.INTERNAL_NAME, "setGameInstance",
							"(Ljava/lang/Object;)V"));
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
	public @NotNull Set<Target<MethodNode>> targets() {
		return Set.of(Target.targetMethod("net.minecraft.server.Main", "main", "([Ljava/lang/String)V"));
	}

	@Override
	public @NotNull TargetType<MethodNode> getTargetType() {
		return TargetType.METHOD;
	}
}
