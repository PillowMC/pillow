/*
 * MIT License
 *
 * Copyright (c) 2024 PillowMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pillowmc.pillow.asm;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IRemapper;

@Deprecated(forRemoval = true)
public class RemapModTransformer implements ITransformer<ClassNode> {
	private final IRemapper remapper;

	RemapModTransformer() {
		// var mappings =
		// QuiltLauncherBase.getLauncher().getMappingConfiguration().getMappings();
		// remapper = RemapperUtils.create(mappings, PillowNamingContext.fromName,
		// PillowNamingContext.toName);
		remapper = MixinEnvironment.getDefaultEnvironment().getRemappers();
	}

	@Override
	public @NotNull ClassNode transform(ClassNode input, ITransformerVotingContext context) {
		if (input.invisibleAnnotations == null)
			return input;
		String cn = null;
		for (AnnotationNode node : input.invisibleAnnotations) {
			if (node.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")) {
				var v = node.values.get(node.values.indexOf("value") + 1);
				if (v instanceof List<?> l) {
					cn = ((Type) l.get(0)).getInternalName();
				} else {
					v = node.values.get(node.values.indexOf("targets") + 1);
					if (v instanceof List<?> l)
						cn = (String) l.get(0);
					else
						throw new AssertionError();
				}
				break;
			}
		}
		if (null == cn)
			return input;
		var cnSrg = remapper.map(cn);
		for (FieldNode node : input.fields) {
			if (node.visibleAnnotations == null)
				continue;
			for (AnnotationNode ann : node.visibleAnnotations) {
				if (ann.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;")) {
					node.name = remapper.mapFieldName(cn, node.name, node.desc);
				}
			}
		}
		for (MethodNode node : input.methods) {
			if (node.visibleAnnotations == null)
				continue;
			for (AnnotationNode ann : node.visibleAnnotations) {
				if ("Lorg/spongepowered/asm/mixin/Overwrite;".equals(ann.desc)) {
					node.name = remapper.mapMethodName(cn, node.name, node.desc);
				} else if (ann.desc.startsWith("Lorg/spongepowered/asm/mixin/injection")) {
					var methods = (List<String>) ann.values.get(ann.values.indexOf("method") + 1);
					for (int i = 0; i < methods.size(); i++) {
						methods.set(i, cnSrg + "." + methods.get(i));
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
		return Collections.emptySet();
		// return
		// ModJarProcessor.classes.stream().map(Target::targetPreClass).collect(Collectors.toSet());
	}
}
