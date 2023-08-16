package net.pillowmc.pillow.launch;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream.Builder;

import net.minecraftforge.fml.loading.VersionInfo;
import net.minecraftforge.fml.loading.targets.CommonClientLaunchHandler;

public class PillowClientLaunchHandler extends CommonClientLaunchHandler {
    @Override public String name() { return "pillowclient"; }

    @Override
    protected void processMCStream(VersionInfo versionInfo, Builder<Path> mc, Builder<List<Path>> mods) {
        // var forgejar = LibraryFinder.findPathForMaven("net.neoforged", "forge", "", "universal", versionInfo.mcAndForgeVersion());
        // mods.add(List.of(forgejar));
    }
}
