package net.pillowmc.pillow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.quiltmc.loader.api.ModContainer;

import net.pillowmc.pillow.asm.PillowNamingContext;

public final class ModJarProcessor {
    public static final Set<String> classes=new HashSet<>();
    private ModJarProcessor() {}
    public static Path createVirtualModJar(ModContainer modFile, Function<Path, List<Path>> pathProcesser) throws IOException{
        File output = File.createTempFile(modFile.metadata().id()+".virtual-", "-"+modFile.metadata().version().raw()+".jar");
        JarOutputStream outJar=new JarOutputStream(new FileOutputStream(output));
        var i=modFile.rootPath();
        Files.walk(i).forEach(path -> {
            try {
                path = path.toAbsolutePath();
                if (path.startsWith("/META-INF/services")) return;
                if (Files.isDirectory(path))
                    outJar.putNextEntry(new JarEntry(path.toAbsolutePath().toString()));
                else if (!path.toString().endsWith(".class")) {
                    outJar.putNextEntry(new JarEntry(path.toAbsolutePath().toString()));
                    outJar.write(Files.readAllBytes(path));
                } else {
                    String name=path.toString();
                    if(!PillowNamingContext.isUserDev)classes.add(name.substring(1, name.length()-6).replace("/", "."));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        outJar.putNextEntry(new JarEntry("pack.mcmeta"));
        outJar.write(("{\"pack\": {\"pack_format\": 9,\"description\": \""+modFile.metadata().id()+"\"}}").getBytes());
        outJar.close();
        return output.toPath();
    }
}
