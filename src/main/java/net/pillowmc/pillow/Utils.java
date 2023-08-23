package net.pillowmc.pillow;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Predicate;

import org.quiltmc.loader.impl.FormattedException;
import org.quiltmc.loader.impl.entrypoint.EntrypointUtils;
import org.quiltmc.loader.impl.filesystem.QuiltZipFileSystem;
import org.quiltmc.loader.impl.filesystem.QuiltZipPath;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraftforge.fml.loading.FMLLoader;
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
        return side = FMLLoader.getDist().isClient() ? EnvType.CLIENT : EnvType.SERVER;
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
            var old=setModule(pc.getModule(), Utils.class);
            try {
                var fsf=pc.getDeclaredField("fileSystem");
                fsf.setAccessible(true);
                var fs=fsf.get(path);
                var fsc=fs.getClass();
                var bpf=fsc.getDeclaredField("basepaths");
                bpf.setAccessible(true);
                setModule(old, Utils.class);
                return (List<Path>)bpf.get(fs);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
        return List.of(path);
    }

    public static Path extractZipPath(Path path) {
        if (path instanceof QuiltZipPath zp) {
            var fs = zp.getFileSystem();
            var fsc = QuiltZipFileSystem.class;
            // var old=setModule(fsc.getModule(), Utils.class);
            try {
                var channelsField=fsc.getDeclaredField("source");
                channelsField.setAccessible(true);
                var channels=channelsField.get(fs);
                var channelClass=channels.getClass();
                var zipFromField=channelClass.getDeclaredField("zipFrom");
                zipFromField.setAccessible(true);
                return (Path)zipFromField.get(channels);
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                throw new RuntimeException(e);
            }
        }
        return path;
    }

    public static Module setModule(Module new_, Class<?> class_) {
        var old = class_.getModule();
        unsafe.putObject(class_, offset, new_);
        return old;
    }
    
    @FunctionalInterface
    public static interface PredicateThrowable<T, E extends Throwable> {
        /**
         * Evaluates this predicate on the given argument.
         *
         * @param t the input argument
         * @return {@code true} if the input argument matches the predicate,
         * otherwise {@code false}
         */
        boolean test(T t) throws E;
    }

    public static <T, E extends Throwable> Predicate<T> rethrowPredicate(PredicateThrowable<T, E> perdicateThrowable) {
        return (v) -> {
            try {
                return perdicateThrowable.test(v);
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }
}
