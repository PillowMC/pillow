package net.pillowmc.pillow.asm;

import cpw.mods.jarhandling.SecureJar;
import cpw.mods.modlauncher.api.*;
import cpw.mods.niofs.union.UnionPath;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.launch.common.FabricLauncherBase;
import net.fabricmc.loader.launch.common.FabricMixinBootstrap;
import net.pillowmc.pillow.PillowGameProvider;
import net.pillowmc.pillow.Utils;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.impl.FormattedException;
import org.quiltmc.loader.impl.QuiltLoaderImpl;
import org.quiltmc.loader.impl.entrypoint.EntrypointUtils;
import org.quiltmc.loader.impl.filesystem.QuiltJoinedFileSystemProvider;
import org.quiltmc.loader.impl.filesystem.QuiltMemoryFileSystemProvider;
import org.quiltmc.loader.impl.game.GameProvider;
import org.quiltmc.loader.impl.util.SystemProperties;
import org.quiltmc.loader.impl.util.log.Log;
import org.quiltmc.loader.impl.util.log.LogCategory;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class PillowTransformationService extends FabricLauncherBase implements ITransformationService {
    private static Unsafe unsafe;
    private final Path remapCP = Files.createTempFile("pillowremapping", ".cp");
    private static Field module;
    static {
        Field theUnsafe;
        try {
            theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
            module = Class.class.getDeclaredField("module");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public PillowTransformationService() throws IOException {
    }

    @Override
    public @NotNull String name() {
        return "pillow";
    }

    @Override
    public void initialize(IEnvironment environment) {
        System.setProperty(SystemProperties.MODS_DIRECTORY, "quiltmods");
        System.setProperty(SystemProperties.REMAP_CLASSPATH_FILE, remapCP.toString());
        provider = new PillowGameProvider();
        provider.locateGame(this, new String[0]);
        Log.info(LogCategory.GAME_PROVIDER, "Loading %s %s with Quilt Loader %s", provider.getGameName(), provider.getRawGameVersion(), QuiltLoaderImpl.VERSION);
        provider.initialize(this);
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {

    }

    @Override
    public @NotNull List<ITransformer> transformers() {
        return Collections.emptyList();
    }

    @Override
    public List<Resource> beginScanning(IEnvironment environment) {
        try {
            long offset = unsafe.objectFieldOffset(module);
            unsafe.putObject(PillowTransformationService.class, offset, FileSystemProvider.class.getModule());
            Field field=FileSystemProvider.class.getDeclaredField("installedProviders");
            field.setAccessible(true);
            List<FileSystemProvider> providers= (List<FileSystemProvider>) field.get(null), newProviders;
            newProviders=providers.stream().filter((provider_)->!(provider_.getClass().getName().contains("Quilt")))
                    .collect(Collectors.toList());
            newProviders.add(new QuiltMemoryFileSystemProvider());
            newProviders.add(new QuiltJoinedFileSystemProvider());
            field.set(null, newProviders);
            return Collections.singletonList(new Resource(IModuleLayerManager.Layer.GAME,
                    Collections.singletonList(
                            SecureJar.from(getUFSPath(Paths.get(QuiltLoaderImpl.class.getProtectionDomain().getCodeSource().getLocation().toURI())))
                        )
                    ));
        } catch (URISyntaxException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getUFSPath(Path path) {
        if(path.getClass().getName().contains("Union")){
            FileSystem fs=path.getFileSystem();
            Class<?> fsc = fs.getClass();
            try {
                long offset = unsafe.objectFieldOffset(module);
                unsafe.putObject(PillowTransformationService.class, offset, fsc.getModule());
                return (Path) fsc.getMethod("getPrimaryPath").invoke(fs);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return path;
    }

    @Override
    public List<Resource> completeScan(IModuleLayerManager layerManager) {
        try {
            Files.writeString(this.remapCP, cp.stream().map(Path::toString).collect(Collectors.joining(File.pathSeparator)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        QuiltLoaderImpl loader = QuiltLoaderImpl.INSTANCE;
        loader.setGameProvider(provider);
        loader.load();
        loader.freeze();
        QuiltLoaderImpl.INSTANCE.loadAccessWideners();
        FabricMixinBootstrap.init(getEnvironmentType(), loader);
        finishMixinBootstrapping();
        provider.unlockClassPath(this);
        try {
            EntrypointUtils.invoke("pre_launch", org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint.class, org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint::onPreLaunch);
            EntrypointUtils.invoke("preLaunch", net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint.class, net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint::onPreLaunch);
        } catch (RuntimeException e) {
            throw new FormattedException("A mod crashed on startup!", e);
        }
        return Collections.singletonList(new Resource(IModuleLayerManager.Layer.GAME, cp.stream()
                .map(SecureJar::from)
                .collect(Collectors.toList())
        ));
    }

    private GameProvider provider;
    private final List<Path> cp=new ArrayList<>();

    // FabricLauncher start

    @Override
    public void addToClassPath(Path path, String... allowedPrefixes) {
        cp.add(path);
    }

    @Override
    public void setAllowedPrefixes(Path path, String... prefixes) {}

    @Override
    public EnvType getEnvironmentType() {
        return Utils.getSide();
    }

    @Override
    public boolean isClassLoaded(String name) {
        return false;
    }

    @Override
    public Class<?> loadIntoTarget(String name) {
        return null;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }

    @Override
    public ClassLoader getTargetClassLoader() {
        return this.getClass().getClassLoader();
    }

    @Override
    public byte[] getClassByteArray(String name, boolean runTransformers) {
        return new byte[0];
    }

    @Override
    public Manifest getManifest(Path originPath) {
        return null;
    }

    @Override
    public boolean isDevelopment() {// For remapping mods
        return true;
    }

    @Override
    public String getEntrypoint() {
        return provider.getEntrypoint();
    }

    @Override
    public String getTargetNamespace() {
        return "searge";
    }

    @Override
    public List<Path> getClassPath() {
        return null;
    }
}
