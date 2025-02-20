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
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.lib.accesswidener.AccessWidener;
import net.fabricmc.loader.impl.lib.accesswidener.AccessWidenerClassVisitor;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.ClassNode;

public class AWTransformer implements ITransformer<ClassNode> {
	private final AccessWidener aw;
	public AWTransformer() {
		aw = FabricLoaderImpl.INSTANCE.getAccessWidener();
	}

	@Override
	public @NotNull ClassNode transform(ClassNode input, ITransformerVotingContext context) {
		ClassNode output = new ClassNode(FabricLoaderImpl.ASM_VERSION);
		ClassVisitor visitor = AccessWidenerClassVisitor.createClassVisitor(FabricLoaderImpl.ASM_VERSION, output,
				FabricLoaderImpl.INSTANCE.getAccessWidener());
		input.accept(visitor);
		return output;
	}

	@Override
	public @NotNull TransformerVoteResult castVote(ITransformerVotingContext context) {
		return TransformerVoteResult.YES;
	}

	@Override
	public @NotNull Set<Target<ClassNode>> targets() {
		return aw.getTargets().stream().map(i -> i.replace(".", "/"))
				.map(name -> ITransformer.Target.targetPreClass(name.replace('/', '.'))).collect(Collectors.toSet());
	}

	@Override
	public @NotNull TargetType<ClassNode> getTargetType() {
		return TargetType.PRE_CLASS;
	}
}
