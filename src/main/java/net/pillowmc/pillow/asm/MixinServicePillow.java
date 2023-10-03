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

package net.pillowmc.pillow.asm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.quiltmc.loader.api.FasterFiles;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;
import org.quiltmc.loader.impl.util.log.Log;
import org.quiltmc.loader.impl.util.log.LogCategory;
import org.spongepowered.asm.service.modlauncher.MixinServiceModLauncher;

public class MixinServicePillow extends MixinServiceModLauncher {

	public MixinServicePillow() throws URISyntaxException {
		super();
		this.getPrimaryContainer().addResource("pillow", Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()));
	}

    @Override
    public String getName() {
        return "ModLauncher/Pillow";
    }

    @Override
    public InputStream getResourceAsStream(String name) {
		if (name.startsWith("#")) {
			// Probably a mod specific resource
			int colon = name.indexOf(':');
			if (colon > 0) {
				String mod = name.substring(1, colon);
				String resource = name.substring(colon + 1);
				Optional<ModContainer> modContainer = QuiltLoader.getModContainer(mod);
				if (modContainer.isPresent()) {
					Path modResource = modContainer.get().rootPath().resolve(resource);
					try {
						if (!FasterFiles.exists(modResource)) {
							URL url = QuiltLauncherBase.getLauncher().getResourceURL(resource);
							if (url != null) {
								Log.warn(LogCategory.GENERAL, "Failed to find the resource '" + resource + "' in mod '" + mod + "', but did find it in a different place: " + url);
								return url.openStream();
							}
							return null;
						}
						return Files.newInputStream(modResource);
					} catch (IOException e) {
						throw new RuntimeException("Failed to read file '" + resource + "' from mod '" + mod + "'!", e);
					}
				}
			}
		}
		return QuiltLauncherBase.getLauncher().getResourceAsStream(name);
    }
}
