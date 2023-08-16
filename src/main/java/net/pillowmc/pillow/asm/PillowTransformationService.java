package net.pillowmc.pillow.asm;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Manifest;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.impl.QuiltLoaderImpl;
import org.quiltmc.loader.impl.filesystem.DelegatingUrlStreamHandlerFactory;
import org.quiltmc.loader.impl.filesystem.QuiltJoinedFileSystemProvider;
import org.quiltmc.loader.impl.filesystem.QuiltMemoryFileSystemProvider;
import org.quiltmc.loader.impl.filesystem.QuiltZipFileSystemProvider;
import org.quiltmc.loader.impl.game.GameProvider;
import org.quiltmc.loader.impl.game.minecraft.Log4jLogHandler;
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;
import org.quiltmc.loader.impl.plugin.gui.I18n;
import org.quiltmc.loader.impl.util.log.Log;
import org.quiltmc.loader.impl.util.log.LogCategory;

import cpw.mods.jarhandling.JarMetadata;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.jarhandling.impl.SimpleJarMetadata;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.IModuleLayerManager;
import cpw.mods.modlauncher.api.IModuleLayerManager.Layer;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import net.fabricmc.api.EnvType;
import net.pillowmc.pillow.PillowGameProvider;
import net.pillowmc.pillow.Utils;
import net.pillowmc.pillow.asm.qsl.itemsettings.LivingEntityMixinTransformer;
import net.pillowmc.pillow.asm.qsl.resourceloader.MinecraftClientMixinTransformer;
import sun.misc.Unsafe;

public class PillowTransformationService extends QuiltLauncherBase implements ITransformationService {
    public static Unsafe unsafe;
    public static long offset;
    static {
        Field theUnsafe;
        try {
            theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
            Field module = Class.class.getDeclaredField("module");
            offset = unsafe.objectFieldOffset(module);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public PillowTransformationService() {
        var layer = Launcher.INSTANCE.findLayerManager().get().getLayer(Layer.BOOT).get();
        Utils.setModule(Thread.currentThread().getContextClassLoader().getUnnamedModule(), I18n.class);
        try {
            var field = layer.getClass().getDeclaredField("servicesCatalog");
            var old = Utils.setModule(layer.getClass().getModule(), PillowTransformationService.class);
            field.setAccessible(true);
            var catalog = field.get(layer);
            var mapField = catalog.getClass().getDeclaredField("map");
            mapField.setAccessible(true);
            var map = (Map<String, List<Object>>)mapField.get(catalog);
            map.get("org.spongepowered.asm.service.IMixinService").removeIf((v) -> 
                {
                    try {
                        return "org.quiltmc.loader".equals(((Module)v.getClass().getMethod("module").invoke(v)).getName());
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                            | NoSuchMethodException | SecurityException e) {
                        throw new RuntimeException(e);
                    }
                }
            );
            map.get("org.spongepowered.asm.service.IMixinServiceBootstrap").removeIf((v) -> 
                {
                    try {
                        return "org.quiltmc.loader".equals(((Module)v.getClass().getMethod("module").invoke(v)).getName());
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                            | NoSuchMethodException | SecurityException e) {
                        throw new RuntimeException(e);
                    }
                }
            );
            map.get("org.spongepowered.asm.service.IGlobalPropertyService").removeIf((v) -> 
                {
                    try {
                        return "org.quiltmc.loader".equals(((Module)v.getClass().getMethod("module").invoke(v)).getName());
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                            | NoSuchMethodException | SecurityException e) {
                        throw new RuntimeException(e);
                    }
                }
            );
            Utils.setModule(old, PillowTransformationService.class);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull String name() {
        return "pillow";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(IEnvironment environment) {
        setupUncaughtExceptionHandler();
        // Don't touch here
        try {
            var old = getClass().getModule();
            unsafe.putObject(getClass(), offset, URL.class.getModule());
            var field = URL.class.getDeclaredField("factory");
            field.setAccessible(true);
            var oldFactory = (URLStreamHandlerFactory) field.get(null);
            field.set(null, null);
            DelegatingUrlStreamHandlerFactory.appendFactory(oldFactory);
            field.set(null, DelegatingUrlStreamHandlerFactory.INSTANCE);
            unsafe.putObject(getClass(), offset, old);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // End of don't touch here
        try {
            var FSPC = FileSystemProvider.class;
            var old = getClass().getModule();
            unsafe.putObject(getClass(), offset, FSPC.getModule());
            var installedProviders = FSPC.getDeclaredField("installedProviders");
            installedProviders.setAccessible(true);
            var val = (List<FileSystemProvider>) installedProviders.get(null);
            var newval = new ArrayList<>(val);
            newval.removeIf(i -> i.getClass().getName().contains("Quilt"));
            newval.add(new QuiltMemoryFileSystemProvider());
            newval.add(new QuiltJoinedFileSystemProvider());
            newval.add(new QuiltZipFileSystemProvider());
            installedProviders.set(null, Collections.unmodifiableList(newval));
            unsafe.putObject(getClass(), offset, old);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            Launcher.INSTANCE.environment().findLaunchPlugin("mixin").orElseThrow().offerResource(
                    Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()),
                    "pillow");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        provider = new PillowGameProvider();
        Log.init(new Log4jLogHandler(), true);
        provider.locateGame(this, new String[0]);
        Log.info(LogCategory.GAME_PROVIDER, "Loading %s %s with Quilt Loader %s", provider.getGameName(),
                provider.getRawGameVersion(), QuiltLoaderImpl.VERSION);
        provider.initialize(this);
        QuiltLoaderImpl loader = QuiltLoaderImpl.INSTANCE;
        loader.setGameProvider(provider);
        loader.load();
        loader.freeze();
        QuiltLoaderImpl.INSTANCE.loadAccessWideners();
    }

    @Override
    public List<Resource> completeScan(IModuleLayerManager environment) {
        Log.debug(LogCategory.DISCOVERY, "Completing scan with classpath %s", cp);
        if (cp.isEmpty())
            return List.of();
        return List.of(new Resource(Layer.GAME,
                List.of(SecureJar.from(sj -> PillowTransformationService.createJarMetadata(sj, "quiltMods"),
                        cp.toArray(new Path[0])))));
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {

    }

    @Override
    @SuppressWarnings("rawtypes")
    public @NotNull List<ITransformer> transformers() {
        return List.of(
                Utils.getSide() == EnvType.CLIENT ? new ClientEntryPointTransformer()
                        : new ServerEntryPointTransformer(),
                new AWTransformer(), new ModListScreenTransformer(), new RemapModTransformer(),
                new MinecraftClientMixinTransformer(), new LivingEntityMixinTransformer());
    }

    public static JarMetadata createJarMetadata(SecureJar sj, String name) {
        return new SimpleJarMetadata(name, "1.0.0", sj.getPackages(), sj.getProviders());
    }

    private GameProvider provider;
    private final List<Path> cp = new ArrayList<>();
    private static List<String> NO_ADDING = List.of("pillow", "forge", "minecraft");

    // QuiltLauncher start

    @Override
    public void addToClassPath(Path path, String... allowedPrefixes) {
        var name = Utils.extractZipPath(path);
        try {
            if (!name.getClass().getName().contains("Union")
                    && !name.equals(Utils
                            .extractUnionPaths(
                                    Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()))
                            .get(0)))
                cp.add(name);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setAllowedPrefixes(Path path, String... prefixes) {
    }

    @Override
    public EnvType getEnvironmentType() {
        return Utils.getSide();
    }

    @Override
    public boolean isClassLoaded(String name) {
        return false;
    }

    @Override
    public Class<?> loadIntoTarget(String name) throws ClassNotFoundException {
        return getTargetClassLoader().loadClass(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return this.getClass().getClassLoader().getResourceAsStream(name);
    }

    @Override
    public ClassLoader getTargetClassLoader() {
        return Thread.currentThread().getContextClassLoader();
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
    public boolean isDevelopment() {
        return Thread.currentThread().getStackTrace()[2].getClassName().contains("ClasspathModCandidateFinder");
    }

    @Override
    public String getEntrypoint() {
        return provider.getEntrypoint();
    }

    @Override
    public String getTargetNamespace() {
        return "intermediary"; // Trick Quilt
    }

    @Override
    public List<Path> getClassPath() {
        return null;
    }

    @Override
    public void addToClassPath(Path path, ModContainer mod, URL origin, String... allowedPrefixes) {
        if (NO_ADDING.contains(mod.metadata().id())) return;
        addToClassPath(path, allowedPrefixes);
    }

    @Override
    public void setTransformCache(URL insideTransformCache) {
    }

    @Override
    public void hideParentUrl(URL hidden) {
    }

    @Override
    public void hideParentPath(Path obf) {
    }

    @Override
    public void validateGameClassLoader(Object gameInstance) {
    }

    @Override
    public URL getResourceURL(String name) {
        return getTargetClassLoader().getResource(name);
    }

    @Override
    public ClassLoader getClassLoader(ModContainer mod) {
        return getTargetClassLoader();
    }
}
