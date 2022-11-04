package net.pillowmc.pillow;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLServiceProvider;
import org.quiltmc.loader.impl.entrypoint.GameTransformer;
import org.quiltmc.loader.impl.game.GameProvider;
import org.quiltmc.loader.impl.launch.common.QuiltLauncher;
import org.quiltmc.loader.impl.metadata.BuiltinModMetadata;
import org.quiltmc.loader.impl.metadata.ModDependencyImpl;
import org.quiltmc.loader.impl.util.Arguments;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PillowGameProvider implements GameProvider {
    private String[] args;

    @Override
    public String getGameId() {
        return "minecraft";
    }

    @Override
    public String getGameName() {
        return "Minecraft";
    }

    @Override
    public String getRawGameVersion() {
        return FMLLoader.versionInfo().mcVersion();
    }

    @Override
    public String getNormalizedGameVersion() {
        return getRawGameVersion();
    }

    @Override
    public Collection<BuiltinMod> getBuiltinMods() {
        BuiltinModMetadata.Builder minecraftMetadata = new BuiltinModMetadata.Builder(getGameId(), getNormalizedGameVersion())
                .setName(getGameName())
                .setDescription("Obfuscated as Searge name, MCP version = %s".formatted(FMLLoader.versionInfo().mcpVersion()));
        try {
            minecraftMetadata.addDependency(new ModDependencyImpl(ModDependency.Kind.DEPENDS, "java", List.of(String.format(">=%d", 17))));
        } catch (VersionParsingException e) {
            throw new RuntimeException(e);
        }
        BuiltinModMetadata.Builder forgeMetadata = new BuiltinModMetadata.Builder("forge", FMLLoader.versionInfo().forgeVersion())
                .setName("forge");
        try {
            forgeMetadata.addDependency(new ModDependencyImpl(ModDependency.Kind.DEPENDS, getGameId(), List.of(String.format("=%s", getRawGameVersion()))));
        } catch (VersionParsingException e) {
            throw new RuntimeException(e);
        }
        try {
            var path=FMLServiceProvider.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            return Arrays.asList(new BuiltinMod(List.of(Paths.get(path)), minecraftMetadata.build()),
                    new BuiltinMod(List.of(Paths.get(path)), forgeMetadata.build())
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getEntrypoint() {
        if(Utils.getSide()== EnvType.CLIENT){
            return "net.minecraft.client.main.Main";
        }else{
            return "net.minecraft.server.Main";
        }
    }

    @Override
    public Path getLaunchDirectory() {
        return FMLLoader.getGamePath();
    }

    @Override
    public boolean isObfuscated() {
        return true;
    }

    @Override
    public boolean requiresUrlClassLoader() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean locateGame(QuiltLauncher launcher, String[] args) {
        this.args=args;
        return true;
    }

    @Override
    public void initialize(QuiltLauncher launcher) {
    }

    @Override
    public GameTransformer getEntrypointTransformer() {
        return null;
    }

    @Override
    public void unlockClassPath(QuiltLauncher launcher) {

    }

    @Override
    public void launch(ClassLoader loader) {

    }

    @Override
    public Arguments getArguments() {
        Arguments arguments=new Arguments();
        arguments.parse(args);
        return arguments;
    }

    @Override
    public String[] getLaunchArguments(boolean sanitize) {
        return new String[0];
    }
}
