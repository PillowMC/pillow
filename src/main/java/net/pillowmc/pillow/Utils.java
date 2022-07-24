package net.pillowmc.pillow;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;

import org.quiltmc.loader.impl.FormattedException;
import org.quiltmc.loader.impl.entrypoint.EntrypointUtils;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import sun.misc.Unsafe;

public class Utils {
    private static EnvType side;
    private static Unsafe unsafe;
    private static long offset;
    static {
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
    public static EnvType getSide() {
        if(side!=null)return side;
        return side=Launcher.INSTANCE.environment().getProperty(IEnvironment.Keys.LAUNCHTARGET.get())
                .orElseThrow(()->new IllegalStateException("ModLauncher initializes \"ITransformingService\"s without launchTarget! WON'T POSSIBLE!"))
                .contains("client") ? EnvType.CLIENT : EnvType.SERVER;
    }
    @SuppressWarnings("deprecation") // Used by transformed classes.
    public static void preLaunch(){
        try {
            EntrypointUtils.invoke("pre_launch", org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint.class, org.quiltmc.loader.api.entrypoint.PreLaunchEntrypoint::onPreLaunch);
            EntrypointUtils.invoke("preLaunch", PreLaunchEntrypoint.class, PreLaunchEntrypoint::onPreLaunch);
        } catch (RuntimeException e) {
            throw new FormattedException("A mod crashed on startup!", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Path> extractUnionPaths(Path path) {
        if(path.getClass().getName().contains("Union")){
            var pc=path.getClass();
            var old=Utils.class.getModule();
            unsafe.putObject(Utils.class, offset, pc.getModule());
            try {
                var fsf=pc.getDeclaredField("fileSystem");
                fsf.setAccessible(true);
                var fs=fsf.get(path);
                var fsc=fs.getClass();
                var bpf=fsc.getDeclaredField("basepaths");
                bpf.setAccessible(true);
                unsafe.putObject(Utils.class, offset, old);
                return (List<Path>)bpf.get(fs);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
        return List.of(path);
    }

    public static Path extractZipPath(Path path) {
        if(path.getClass().getName().contains("Zip")){
            var pc=path.getClass();
            var old= Utils.class.getModule();
            unsafe.putObject(Utils.class, offset, pc.getModule());
            try {
                var zfsf=pc.getDeclaredField("zfs");
                zfsf.setAccessible(true);
                var zfs=zfsf.get(path);
                var zfsc=zfs.getClass();
                var zfpf=zfsc.getDeclaredField("zfpath");
                zfpf.setAccessible(true);
                unsafe.putObject(Utils.class, offset, old);
                return (Path)zfpf.get(zfs);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
        return path;
    }
}
