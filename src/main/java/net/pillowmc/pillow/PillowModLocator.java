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

package net.pillowmc.pillow;

import com.electronwill.nightconfig.core.Config;
import cpw.mods.jarhandling.VirtualJar;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.fml.loading.moddiscovery.NightConfigWrapper;
import net.neoforged.neoforgespi.ILaunchContext;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;

public class PillowModLocator implements IModFileCandidateLocator {

	private record ProvidedModInfo(String id, String version) {
	}

	@Override
	public void findCandidates(ILaunchContext context, IDiscoveryPipeline pipeline) {
		var providedMods = new HashMap<net.fabricmc.loader.api.ModContainer, ArrayList<ProvidedModInfo>>();

		FabricLoader.getInstance().getAllMods().forEach((mod) -> {
			mod.getOrigin().getPaths().forEach(context::addLocated);

			for (var e : mod.getMetadata().getCustomValue("pillow-provided").getAsObject()) {
				providedMods.computeIfAbsent(mod, (v) -> new ArrayList<>())
						.add(new ProvidedModInfo(e.getKey(), e.getValue().getAsString()));
			}
		});

		for (var e : providedMods.entrySet()) {
			pipeline.addModFile(IModFile.create(
					new VirtualJar(e.getKey().getMetadata().getId().replace('-', '_'), getRefPath(e.getKey())),
					(mf) -> {
						var config = genModFileInfoConf(e.getKey(), e.getValue());
						return new ModFileInfo((ModFile) mf, config, config::setFile, List.of());
					}, IModFile.Type.MOD, ModFileDiscoveryAttributes.DEFAULT));
		}
	}

	private static NightConfigWrapper genModFileInfoConf(ModContainer mod, ArrayList<ProvidedModInfo> providedMods) {
		final var conf = Config.inMemory();
		conf.set("modLoader", "lowcode");
		conf.set("loaderVersion", "[0,)");
		conf.set("license", joinLicenses(mod.getMetadata().getLicense()));
		var mods = new ArrayList<Config>();
		for (var providedMod : providedMods) {
			final var modConf = Config.inMemory();
			modConf.set("modId", providedMod.id);
			modConf.set("version", providedMod.version);
			modConf.set("displayName", providedMod.id);
			modConf.set("description", providedMod.id);
			mods.add(modConf);
		}
		conf.set("mods", mods);

		return new NightConfigWrapper(conf);
	}

	private static String joinLicenses(Collection<String> c) {
		if (c.isEmpty()) {
			return "All Rights Reserved";
		}
		return String.join(", ", c);
	}

	private static Path getRefPath(ModContainer mod) {
		return mod.getOrigin().getPaths().getLast();
	}

}
