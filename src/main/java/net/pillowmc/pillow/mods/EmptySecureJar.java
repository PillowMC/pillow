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

package net.pillowmc.pillow.mods;

import cpw.mods.jarhandling.SecureJar;

import java.io.InputStream;
import java.lang.module.ModuleDescriptor;
import java.net.URI;
import java.nio.file.Path;
import java.security.CodeSigner;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static cpw.mods.modlauncher.api.LamdbaExceptionUtils.uncheck;

public class EmptySecureJar implements SecureJar {
    private String name;

    private class EmptyModuleDataProvider implements ModuleDataProvider {
        private ModuleDescriptor descriptor;

        @Override
        public String name() {
            return EmptySecureJar.this.name;
        }

        @Override
        public ModuleDescriptor descriptor() {
            if (descriptor == null) {
                descriptor = ModuleDescriptor.newAutomaticModule(EmptySecureJar.this.name)
                    .build();
            }
            return descriptor;
        }

        @Override
        public URI uri() {
            return uncheck(() -> new URI("file:///~nonexistent"));
        }

        @Override
        public Optional<URI> findFile(String name) {
            return Optional.empty();
        }

        @Override
        public Optional<InputStream> open(String name) {
            return Optional.empty();
        }

        @Override
        public Manifest getManifest() {
            return new Manifest();
        }

        @Override
        public CodeSigner[] verifyAndGetSigners(String cname, byte[] bytes) {
            return new CodeSigner[0];
        }
    }

    public EmptySecureJar(String name) {
        this.name = name;
    }

    private final ModuleDataProvider moduleDataProvider = new EmptyModuleDataProvider();

    @Override
    public ModuleDataProvider moduleDataProvider() {
        return moduleDataProvider;
    }

    @Override
    public Path getPrimaryPath() {
        return Path.of(moduleDataProvider().uri());
    }

    @Override
    public CodeSigner[] getManifestSigners() {
        return new CodeSigner[0];
    }

    @Override
    public Status verifyPath(Path path) {
        return Status.NONE;
    }

    @Override
    public Status getFileStatus(String name) {
        return Status.NONE;
    }

    @Override
    public Attributes getTrustedManifestEntries(String name) {
        return new Attributes();
    }

    @Override
    public boolean hasSecurityData() {
        return false;
    }

    @Override
    public Set<String> getPackages() {
        return moduleDataProvider().descriptor().packages();
    }

    @Override
    public List<Provider> getProviders() {
        return List.of();
    }

    @Override
    public String name() {
        return moduleDataProvider().name();
    }

    @Override
    public Path getPath(String first, String... rest) {
        return getPrimaryPath();
    }

    @Override
    public Path getRootPath() {
        return getPrimaryPath();
    }
}