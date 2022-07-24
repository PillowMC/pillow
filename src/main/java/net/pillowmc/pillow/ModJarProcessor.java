package net.pillowmc.pillow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
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
        pathProcesser.apply(i).forEach(j->{
            try {
                var in=new JarInputStream(j.getFileSystem().provider().newInputStream(j));
                var next=in.getNextJarEntry();
                while(next!=null){
                    if(next.getName().startsWith("META-INF/services")){
                        next=in.getNextJarEntry();
                        continue;
                    }
                    if(next.isDirectory())outJar.putNextEntry(next);
                    else if(!next.getName().endsWith(".class")){
                        outJar.putNextEntry(next);
                        outJar.write(in.readAllBytes());
                    }else{
                        String name=next.getName();
                        if(!PillowNamingContext.isUserDev)classes.add(name.substring(0, name.length()-6).replace("/", "."));
                    }
                    next=in.getNextJarEntry();
                }
                in.close();
            } catch (IOException e) {
                // Nothing to do.
            }
        });
        outJar.putNextEntry(new JarEntry("pack.mcmeta"));
        outJar.write(("{\"pack\": {\"pack_format\": 9,\"description\": \""+modFile.metadata().id()+"\"}}").getBytes());
        outJar.close();
        return output.toPath();
    }
}
