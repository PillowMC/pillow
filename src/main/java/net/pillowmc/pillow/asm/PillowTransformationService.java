package net.pillowmc.pillow.asm;

import cpw.mods.jarhandling.JarMetadata;
import cpw.mods.jarhandling.SecureJar;
import cpw.mods.jarhandling.impl.SimpleJarMetadata;
import cpw.mods.modlauncher.api.*;
import cpw.mods.modlauncher.api.IModuleLayerManager.Layer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.util.SystemProperties;
import net.fabricmc.loader.impl.util.log.Log;
import net.fabricmc.loader.impl.util.log.LogCategory;
import net.pillowmc.pillow.PillowGameProvider;
import net.pillowmc.pillow.Utils;

import org.apache.commons.io.file.spi.FileSystemProviders;
import org.jetbrains.annotations.NotNull;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class PillowTransformationService extends FabricLauncherBase implements ITransformationService {
    private static final FileSystemProvider UFSP=FileSystemProviders.installed().getFileSystemProvider("union");
    private static final Class<?> UFSPClass=UFSP.getClass();
    private static final Method UFSPCreate;
    private static Unsafe unsafe;
    private static long offset;
    private final Path remapCP = Files.createTempFile("pillowremapping", ".cp");
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

    public PillowTransformationService() throws IOException {
    }

    @Override
    public @NotNull String name() {
        return "pillow";
    }

    @Override
    public void initialize(IEnvironment environment) {
        setupUncaughtExceptionHandler();
        System.setProperty(SystemProperties.REMAP_CLASSPATH_FILE, remapCP.toString());
        provider = new PillowGameProvider();
        provider.locateGame(this, new String[0]);
        Log.info(LogCategory.GAME_PROVIDER, "Loading %s %s with Fabric Loader %s", provider.getGameName(), provider.getRawGameVersion(), FabricLoaderImpl.VERSION);
        provider.initialize(this);

        try {
            Files.writeString(this.remapCP, cp.stream().map(Path::toString).collect(Collectors.joining(File.pathSeparator)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        FabricLoaderImpl loader = FabricLoaderImpl.INSTANCE;
        loader.setGameProvider(provider);
        loader.load();
        loader.freeze();
        FabricLoaderImpl.INSTANCE.loadAccessWideners();
    }

    // @Override
    // public List<Resource> beginScanning(IEnvironment environment) {
    //     Log.info(LogCategory.DISCOVERY, "Beginning scanning");

    //     try {
    //         return List.of(new Resource(IModuleLayerManager.Layer.GAME,
    //                     List.of(SecureJar.from(Path.of(FabricLauncherBase.class.getProtectionDomain().getCodeSource().getLocation().toURI())))
    //                 ));
    //     } catch (URISyntaxException e) {
    //         throw new RuntimeException(e);
    //     }
    // }
    @Override
    public List<Resource> completeScan(IModuleLayerManager layerManager) {
        Log.info(LogCategory.DISCOVERY, "Completing scan with classpath [%s]", cp);
        List<Path> paths=cp.stream()
            .filter((path)->!path.getClass().getName().contains("Union"))
            .collect(Collectors.toUnmodifiableList());
        return List.of(new Resource(Layer.GAME,
            List.of(SecureJar.from(PillowTransformationService::createJM, mergePaths(paths)))));
    }

    private static Path mergePaths(List<Path> paths){
        unsafe.putObject(PillowTransformationService.class, offset, UFSPClass.getModule());
        try {
            return ((FileSystem)UFSPCreate.invoke(UFSP, (BiPredicate<String, String>)(a, b)->true, paths.toArray(new Path[0]))).getRootDirectories().iterator().next();
        } catch (IllegalAccessException|IllegalArgumentException|InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onLoad(IEnvironment env, Set<String> otherServices) {

    }

    @Override
    public @NotNull List<ITransformer> transformers() {
        return List.of(new EntryPointTransformer(), new AWTransformer());
    }

    private static JarMetadata createJM(SecureJar sj){
        return new SimpleJarMetadata("fabricmod", "1.0.0", sj.getPackages(), sj.getProviders());
    }

    private static Path getUFSPath(Path path) {
        if(path.getClass().getName().contains("Union")){
            FileSystem fs=path.getFileSystem();
            Class<?> fsc = fs.getClass();
            try {
                unsafe.putObject(PillowTransformationService.class, offset, fsc.getModule());
                return (Path) fsc.getMethod("getPrimaryPath").invoke(fs);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
        return path;
    }

    private static Path getZipPath(Path path) {
        if(path.getClass().getName().contains("Zip")){
            unsafe.putObject(PillowTransformationService.class, offset, path.getClass().getModule());
            Class<?> fsc=path.getFileSystem().getClass();
            try {
                Field field=fsc.getDeclaredField("zfpath");
                field.setAccessible(true);
                return (Path) field.get(path.getFileSystem());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return path;
    }

    private GameProvider provider;
    private final List<Path> cp=new ArrayList<>();

    // FabricLauncher start

    @Override
    public void addToClassPath(Path path, String... allowedPrefixes) {
        // try {
        //     unsafe.putObject(PillowTransformationService.class, offset, ClassLoader.getSystemClassLoader().getClass().getModule());
        //     Field f=ClassLoader.getSystemClassLoader().getClass().getSuperclass().getDeclaredField("ucp");
        //     Object ucp=unsafe.getObject(ClassLoader.getSystemClassLoader(), unsafe.objectFieldOffset(f));
        //     ucp.getClass().getMethod("addURL", URL.class).invoke(ucp, path.toUri().toURL());
        // } catch (NoSuchFieldException | NoSuchMethodException | MalformedURLException | IllegalAccessException | InvocationTargetException e) {
        //     throw new RuntimeException(e);
        // }
        cp.add(path);
    }

    @Override
    public void setAllowedPrefixes(Path path, String... prefixes) {}

    @Override
    public void setValidParentClassPath(Collection<Path> paths) {
        // no op
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
        return "named";
    }

    @Override
    public List<Path> getClassPath() {
        return null;
    }
}
