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

package net.pillowmc.pillow.launch;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import net.minecraftforge.fml.loading.VersionInfo;
import net.minecraftforge.fml.loading.targets.CommonServerLaunchHandler;

public class PillowServerLaunchHandler extends CommonServerLaunchHandler {
    @Override public String name() { return "pillowserver"; }

    @Override
    protected BiPredicate<String, String> processMCStream(VersionInfo versionInfo, Stream.Builder<Path> mc, BiPredicate<String, String> filter, Stream.Builder<List<Path>> mods) {
        // var forgejar = LibraryFinder.findPathForMaven("net.neoforged", "forge", "", "universal", versionInfo.mcAndForgeVersion());
        // mods.add(List.of(forgejar));
        return filter;
    }
}
