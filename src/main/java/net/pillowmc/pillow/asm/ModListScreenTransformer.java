/*
 * MIT License
 *
 * Copyright (c) 2023 PillowMC
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

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import cpw.mods.modlauncher.api.INameMappingService.Domain;

public class ModListScreenTransformer implements ITransformer<MethodNode> {
    private static final String m_5541_ = PillowNamingContext.namingFunction.apply(Domain.METHOD, "m_5541_");
    private static final String m_5542_ = PillowNamingContext.namingFunction.apply(Domain.METHOD, "m_5542_");

    @Override
    public @NotNull MethodNode transform(MethodNode input, ITransformerVotingContext context) {
        if(input.name.equals(m_5541_)){
            input.access=Opcodes.ACC_PUBLIC;
            return input;
        }
        input.instructions.forEach(i->{
            if(i instanceof MethodInsnNode min){
                if(min.name.equals(m_5542_)){
                    min.name=m_5541_;
                }
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
        return Set.of(Target.targetMethod("net.minecraftforge.client.gui.ModListScreen", "lambda$updateCache$12", "(Lnet/minecraftforge/forgespi/language/IModInfo;Ljava/lang/String;)Lorg/apache/commons/lang3/tuple/Pair;"),
        Target.targetMethod("net.minecraftforge.resource.PathResourcePack", m_5541_, "(Ljava/lang/String;)Ljava/io/InputStream;"));
    }
    
}
