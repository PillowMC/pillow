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

package net.pillowmc.pillow.hacks;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;

import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;

import net.pillowmc.pillow.Utils;

public class SuperHackyClassLoader extends ClassLoader {
    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (name.equals("quilt.mod.json")) {
            var urls = QuiltLauncherBase.getLauncher().getTargetClassLoader().getResources(name);
            return new Enumeration<URL>() {
                @Override
                public boolean hasMoreElements() {
                    return urls.hasMoreElements();
                }

                @Override
                public URL nextElement() {
                    var url = urls.nextElement();
                    try {
                        return Utils.getUnionPathRealPath(Path.of(url.toURI())).toUri().toURL();
                    } catch (URISyntaxException | MalformedURLException e) {
                        throw new RuntimeException("Can't get path for " + url, e);
                    }
                }
            };
        }
        return Collections.emptyEnumeration();
    }
}
