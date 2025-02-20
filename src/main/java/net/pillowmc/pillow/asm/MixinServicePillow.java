/*
 * Copyright (C) 2025 PillowMC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pillowmc.pillow.asm;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
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
		this.getPrimaryContainer().addResource("pillow",
				Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()));
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
								Log.warn(LogCategory.GENERAL, "Failed to find the resource '" + resource + "' in mod '"
										+ mod + "', but did find it in a different place: " + url);
								return url.openStream();
							}
							return null;
						}
						return Files.newInputStream(modResource);
					} catch (IOException e) {
						throw new UncheckedIOException("Failed to read file '" + resource + "' from mod '" + mod + "'!",
								e);
					}
				}
			}
		}
		return super.getResourceAsStream(name);
	}
}
