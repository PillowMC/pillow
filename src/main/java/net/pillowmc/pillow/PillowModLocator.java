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
import java.util.stream.Collectors;
import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.fml.loading.moddiscovery.NightConfigWrapper;
import net.neoforged.neoforgespi.ILaunchContext;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.ModLicense;
import org.quiltmc.loader.api.QuiltLoader;

public class PillowModLocator implements IModFileCandidateLocator {

	private record ProvidedModInfo(String id, String version) {
	}

	@Override
	public void findCandidates(ILaunchContext context, IDiscoveryPipeline pipeline) {
		var providedMods = new HashMap<org.quiltmc.loader.api.ModContainer, ArrayList<ProvidedModInfo>>();

		QuiltLoader.getAllMods().forEach((mod) -> {
			mod.getSourcePaths().stream().filter((p) -> p.size() == 1) // Exclude JiJ mods.
					.map(List::getFirst).forEach(context::addLocated);

			for (var e : mod.metadata().value("pillow-provided").asObject().entrySet()) {
				providedMods.computeIfAbsent(mod, (v) -> new ArrayList<>())
						.add(new ProvidedModInfo(e.getKey(), e.getValue().asString()));
			}
		});

		for (var e : providedMods.entrySet()) {
			pipeline.addModFile(IModFile.create(
					new VirtualJar(e.getKey().metadata().id().replace('-', '_'), getRefPath(e.getKey())), (mf) -> {
						var config = genModFileInfoConf(e.getKey(), e.getValue());
						return new ModFileInfo((ModFile) mf, config, config::setFile, List.of());
					}, IModFile.Type.MOD, ModFileDiscoveryAttributes.DEFAULT));
		}
	}

	private static NightConfigWrapper genModFileInfoConf(org.quiltmc.loader.api.ModContainer mod,
			ArrayList<ProvidedModInfo> providedMods) {
		final var conf = Config.inMemory();
		conf.set("modLoader", "lowcode");
		conf.set("loaderVersion", "[0,)");
		conf.set("license", joinLicenses(mod.metadata().licenses()));
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

	private static String joinLicenses(Collection<ModLicense> c) {
		if (c.isEmpty()) {
			return "All Rights Reserved";
		}
		return c.stream().map(ModLicense::name).collect(Collectors.joining(", "));
	}

	private static Path getRefPath(ModContainer mod) {
		var src = mod.getSourcePaths().getFirst();
		return src.getLast();
	}

}
