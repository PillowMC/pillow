package net.pillowmc.pillow.asm;

import cpw.mods.jarhandling.JarMetadata;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.jarhandling.impl.SimpleJarMetadata;
import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.*;
import cpw.mods.modlauncher.api.IModuleLayerManager.Layer;
import net.fabricmc.api.EnvType;
import net.pillowmc.pillow.PillowGameProvider;
import net.pillowmc.pillow.Utils;
import net.pillowmc.pillow.asm.qsl.itemsettings.LivingEntityMixinTransformer;
import net.pillowmc.pillow.asm.qsl.resourceloader.MinecraftClientMixinTransformer;

import org.apache.commons.io.file.spi.FileSystemProviders;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.impl.QuiltLoaderImpl;
import org.quiltmc.loader.impl.filesystem.DelegatingUrlStreamHandlerFactory;
import org.quiltmc.loader.impl.filesystem.QuiltJoinedFileSystemProvider;
import org.quiltmc.loader.impl.filesystem.QuiltMemoryFileSystemProvider;
import org.quiltmc.loader.impl.game.GameProvider;
import org.quiltmc.loader.impl.game.minecraft.Log4jLogHandler;
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;
import org.quiltmc.loader.impl.util.log.Log;
import org.quiltmc.loader.impl.util.log.LogCategory;
import sun.misc.Unsafe;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.jar.Manifest;

public class PillowTransformationService extends QuiltLauncherBase implements ITransformationService {
    private static final FileSystemProvider UFSP=FileSystemProviders.installed().getFileSystemProvider("union");
    private static final Class<?> UFSPClass=UFSP.getClass();
    private static final Method UFSPCreate;
    public static Unsafe unsafe;
    public static long offset;
    static {
        try {
            UFSPCreate=UFSPClass.getMethod("newFileSystem", BiPredicate.class, Path[].class);
        } catch (NoSuchMethodException|SecurityException e) {
            throw new RuntimeException(e);
        }
        Field theUnsafe;
        try {
            theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            unsafe = (Unsafe) theUnsafe.get(null);
            Field module = Class.class.getDeclaredField("module");
            offset=unsafe.objectFieldOffset(module);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public PillowTransformationService() {
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
            var old=getClass().getModule();
            unsafe.putObject(getClass(), offset, URL.class.getModule());
            var field=URL.class.getDeclaredField("factory");
            field.setAccessible(true);
            var oldFactory=(URLStreamHandlerFactory) field.get(null);
            field.set(null, null);
            DelegatingUrlStreamHandlerFactory.appendFactory(oldFactory);
            field.set(null, DelegatingUrlStreamHandlerFactory.INSTANCE);
            unsafe.putObject(getClass(), offset, old);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        // End of don't touch here
        try {
            var FSPC = FileSystemProvider.class;
            var old=getClass().getModule();
            unsafe.putObject(getClass(), offset, FSPC.getModule());
            var installedProviders = FSPC.getDeclaredField("installedProviders");
            installedProviders.setAccessible(true);
            var val=(List<FileSystemProvider>)installedProviders.get(null);
            var newval = new ArrayList<>(val);
            newval.removeIf(i->i.getClass().getName().contains("Quilt"));
            newval.add(new QuiltMemoryFileSystemProvider());
            newval.add(new QuiltJoinedFileSystemProvider());
            installedProviders.set(null, Collections.unmodifiableList(newval));
            unsafe.putObject(getClass(), offset, old);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        try {
            Launcher.INSTANCE.environment().findLaunchPlugin("mixin").orElseThrow().offerResource(
                    Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()),
                    "pillow"
            );
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        provider = new PillowGameProvider();
        Log.init(new Log4jLogHandler(), true);
        provider.locateGame(this, new String[0]);
        Log.info(LogCategory.GAME_PROVIDER, "Loading %s %s with Quilt Loader %s", provider.getGameName(), provider.getRawGameVersion(), QuiltLoaderImpl.VERSION);
        provider.initialize(this);
        QuiltLoaderImpl loader = QuiltLoaderImpl.INSTANCE;
        loader.setGameProvider(provider);
        loader.load();
        loader.freeze();
        QuiltLoaderImpl.INSTANCE.loadAccessWideners();
    }

    @Override
    public List<Resource> completeScan(IModuleLayerManager environment) {
        Log.info(LogCategory.DISCOVERY, "Completing scan with classpath %s", cp);
        if(cp.isEmpty())return List.of();
        return List.of(new Resource(Layer.GAME,
            List.of(SecureJar.from(sj->PillowTransformationService.createJM(sj, "quiltMods"), cp.toArray(new Path[0])))));
    }

    public static Path mergePaths(List<Path> paths){
        var old= PillowTransformationService.class.getModule();
        unsafe.putObject(PillowTransformationService.class, offset, UFSPClass.getModule());
        try {
            var val = ((FileSystem)UFSPCreate.invoke(UFSP, (BiPredicate<String, String>)(a, b)->true, paths.toArray(new Path[0]))).getRootDirectories().iterator().next();
            unsafe.putObject(PillowTransformationService.class, offset, old);
            return val;
        } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {

    }

    @Override
    @SuppressWarnings("rawtypes")
    public @NotNull List<ITransformer> transformers() {
        return List.of(Utils.getSide()== EnvType.CLIENT?new ClientEntryPointTransformer():new ServerEntryPointTransformer(), new AWTransformer(), new ModListScreenTransformer(), new RemapModTransformer(), 
        new MinecraftClientMixinTransformer(), new LivingEntityMixinTransformer());
    }

    public static JarMetadata createJM(SecureJar sj, String name){
        return new SimpleJarMetadata(name, "1.0.0", sj.getPackages(), sj.getProviders());
    }

    private GameProvider provider;
    private final List<Path> cp=new ArrayList<>();

    // QuiltLauncher start

    @Override
    public void addToClassPath(Path path, String... allowedPrefixes) {
        var name=Utils.extractZipPath(path);
        try {
            if(!name.getClass().getName().contains("Union")&&!name.equals(Utils.extractUnionPaths(Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI())).get(0)))
                cp.add(name);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
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
        return false;
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
}
