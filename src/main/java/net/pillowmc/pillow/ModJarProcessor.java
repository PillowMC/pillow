package net.pillowmc.pillow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.quiltmc.loader.api.ModContainer;

public final class ModJarProcessor {
    public static final Set<String> classes=new HashSet<>();
    private ModJarProcessor() {}
    public static Path createVirtualModJar(ModContainer modFile, Function<Path, List<Path>> pathProcesser) throws IOException{
        File output = File.createTempFile(modFile.metadata().id()+".virtual-", "-"+modFile.metadata().version().raw()+".jar");
        JarOutputStream outJar=new JarOutputStream(new FileOutputStream(output));
        var i=modFile.rootPath();
        pathProcesser.apply(i).forEach(j->{
            try {
                if(!j.toFile().isFile())return;
                JarFile jar=new JarFile(j.toFile());
                Enumeration<JarEntry> entries=jar.entries();
                while(entries.hasMoreElements()){
                    var next=entries.nextElement();
                    if(next.getName().startsWith("META-INF/services"))continue;
                    if(next.isDirectory())outJar.putNextEntry(next);
                    else if(!next.getName().endsWith(".class")){
                        var in=jar.getInputStream(next);
                        outJar.putNextEntry(next);
                        outJar.write(in.readAllBytes());
                    }else{
                        String name=next.getName();
                        classes.add(name.substring(0, name.length()-6).replace("/", "."));
                    }
                }
                jar.close();
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
