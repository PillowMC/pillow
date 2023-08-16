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
