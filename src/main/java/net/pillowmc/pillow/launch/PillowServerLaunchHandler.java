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

package net.pillowmc.pillow.launch;

import java.util.List;
import java.util.function.Consumer;
import net.neoforged.fml.loading.VersionInfo;
import net.neoforged.fml.loading.targets.CommonServerLaunchHandler;
import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;
import net.pillowmc.pillow.Utils;
import net.pillowmc.pillow.launch.copied.PillowServerProvider;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.QuiltLoaderImpl;
import org.quiltmc.loader.impl.util.log.Log;

public class PillowServerLaunchHandler extends CommonServerLaunchHandler {
	@Override
	public String name() {
		return "pillowserver";
	}

	@Override
	protected String[] preLaunch(String[] arguments, ModuleLayer layer) {
		try {
			// If the very first class transformed by mixin is also referenced by a mixin
			// config
			// then we'll crash due to an "attempted duplicate class definition"
			// Since this target class is *very unlikely* to be referenced by mixin we
			// forcibly load it.
			Thread.currentThread().getContextClassLoader().loadClass("net.minecraft.server.Main");
		} catch (ClassNotFoundException cnfe) {
			Log.warn(Utils.PILLOW_LOG_CATEGORY, "Early non-mixin-config related class failed to load!");
			Log.warn(Utils.PILLOW_LOG_CATEGORY,
					"If you get a 'LinkageError' of 'attempted duplicated * definition' after this then this error is the cause!",
					cnfe);
		}
		QuiltLoaderImpl.INSTANCE.invokePreLaunch();
		return super.preLaunch(arguments, layer);
	}

	@Override
	public void collectAdditionalModFileLocators(VersionInfo versionInfo, Consumer<IModFileCandidateLocator> output) {
		var additionalContent = getAdditionalMinecraftJarContent(versionInfo);
		output.accept(new PillowServerProvider(additionalContent,
				List.of(QuiltLoader.getModContainer("minecraft").orElseThrow().rootPath())));
	}
}
