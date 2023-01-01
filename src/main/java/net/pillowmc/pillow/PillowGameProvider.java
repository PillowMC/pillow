package net.pillowmc.pillow;

import net.fabricmc.api.EnvType;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLServiceProvider;
import org.quiltmc.loader.api.Version;
import org.quiltmc.loader.impl.entrypoint.GameTransformer;
import org.quiltmc.loader.impl.game.GameProvider;
import org.quiltmc.loader.impl.launch.common.QuiltLauncher;
import org.quiltmc.loader.impl.metadata.qmj.V1ModMetadataBuilder;
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
        V1ModMetadataBuilder minecraftMetadata = new V1ModMetadataBuilder();
        minecraftMetadata.id = getGameId();
        minecraftMetadata.version = Version.of(getRawGameVersion());
        minecraftMetadata.name = getGameName();
        minecraftMetadata.description = "Obfuscated as Searge name, MCP version = %s".formatted(FMLLoader.versionInfo().mcpVersion());
        V1ModMetadataBuilder forgeMetadata = new V1ModMetadataBuilder();
        forgeMetadata.id = "forge";
        forgeMetadata.version = Version.of(FMLLoader.versionInfo().forgeVersion());
        forgeMetadata.group = FMLLoader.versionInfo().forgeGroup();
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
        if(Utils.getSide() == EnvType.CLIENT){
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
