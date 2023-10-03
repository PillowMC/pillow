/*
 * MIT License
 *
 * Copyright (c) 2023 PillowMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
