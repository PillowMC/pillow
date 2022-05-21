package net.pillowmc.pillow;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.ModDependency;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLServiceProvider;

import net.fabricmc.loader.impl.game.patch.GameTransformer;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.metadata.BuiltinModMetadata;
import net.fabricmc.loader.impl.metadata.ModDependencyImpl;
import net.fabricmc.loader.impl.util.Arguments;

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
        return "1.18.2";
    }

    @Override
    public String getNormalizedGameVersion() {
        return getRawGameVersion();
    }

    @Override
    public Collection<BuiltinMod> getBuiltinMods() {
        BuiltinModMetadata.Builder minecraftMetadata = new BuiltinModMetadata.Builder(getGameId(), getNormalizedGameVersion())
                .setName(getGameName());
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
            return Arrays.asList(new BuiltinMod(List.of(Paths.get(FMLServiceProvider.class.getProtectionDomain().getCodeSource().getLocation().toURI())), minecraftMetadata.build()),
                    new BuiltinMod(List.of(Paths.get(FMLServiceProvider.class.getProtectionDomain().getCodeSource().getLocation().toURI())), forgeMetadata.build())
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
        return Launcher.INSTANCE.environment()
                .getProperty(IEnvironment.Keys.GAMEDIR.get()).orElse(Path.of("."));
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
    public boolean locateGame(FabricLauncher launcher, String[] args) {
        this.args=args;
        return true;
    }

    @Override
    public void initialize(FabricLauncher launcher) {
    }

    @Override
    public GameTransformer getEntrypointTransformer() {
        return null;
    }

    @Override
    public void unlockClassPath(FabricLauncher launcher) {

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
