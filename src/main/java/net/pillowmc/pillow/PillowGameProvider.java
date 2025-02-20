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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.game.GameProviderHelper;
import net.fabricmc.loader.impl.game.patch.GameTransformer;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.metadata.BuiltinModMetadata;
import net.fabricmc.loader.impl.util.Arguments;
import net.fabricmc.loader.impl.util.SystemProperties;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LibraryFinder;

public class PillowGameProvider implements GameProvider {
	private String[] args;
	private final GameTransformer gameTransformer = new GameTransformer() {
		public byte[] transform(String className) {
			return null;
		}
	};

	@Override
	public String getGameId() {
		return "minecraft";
	}

	@Override
	public String getGameName() {
		return "Minecraft";
	}

	@Override
	public String getRawGameVersion() {
		return FMLLoader.versionInfo().mcVersion();
	}

	@Override
	public String getNormalizedGameVersion() {
		return getRawGameVersion();
	}

	@Override
	public Collection<BuiltinMod> getBuiltinMods() {
		BuiltinModMetadata.Builder minecraftMetadata = new BuiltinModMetadata.Builder(getGameId(),
				getNormalizedGameVersion());
		minecraftMetadata.setName(getGameName());
		minecraftMetadata.setDescription(
				"Deobfuscated, NeoForm version = %s".formatted(FMLLoader.versionInfo().neoFormVersion()));
		Path path;
		var vers = FMLLoader.versionInfo();
		if (Utils.getSide() == EnvType.SERVER) {
			path = LibraryFinder.findPathForMaven("net.minecraft", "server", "", "srg", vers.mcAndNeoFormVersion());
		} else {
			path = LibraryFinder.findPathForMaven("net.minecraft", "client", "", "srg", vers.mcAndNeoFormVersion());
		}
		return List.of(new BuiltinMod(List.of(path), minecraftMetadata.build()));
	}

	@Override
	public String getEntrypoint() {
		if (Utils.getSide() == EnvType.CLIENT) {
			return "net.minecraft.client.main.Main";
		} else {
			return "net.minecraft.server.Main";
		}
	}

	@Override
	public Path getLaunchDirectory() {
		return FMLLoader.getGamePath();
	}

	@Override
	public boolean isObfuscated() {
		return true;
	}

	@Override
	public boolean requiresUrlClassLoader() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean locateGame(FabricLauncher launcher, String[] args) {
		this.args = args;
		return true;
	}

	@Override
	public void initialize(FabricLauncher launcher) {
		var side = Utils.getSide().name().toLowerCase();
		var mc = LibraryFinder.findPathForMaven("net.minecraft", side, "", "slim",
				FMLLoader.versionInfo().mcAndNeoFormVersion());
		Path deobfMC = GameProviderHelper
				.deobfuscate(Map.of(side, mc), getGameId(), getNormalizedGameVersion(), getLaunchDirectory(), launcher)
				.get(side);
		try {
			var pillowdir = FMLPaths.GAMEDIR.get().resolve(".pillow");
			if (!Files.isDirectory(pillowdir)) {
				Files.createDirectory(pillowdir);
			}
			var rcp = pillowdir.resolve("remapClasspath.txt");
			Files.writeString(rcp, deobfMC.toString());
			System.setProperty(SystemProperties.REMAP_CLASSPATH_FILE, rcp.toString());
		} catch (IOException e) {
			throw new UncheckedIOException("Cannot generate remapClasspath!", e);
		}
	}

	@Override
	public GameTransformer getEntrypointTransformer() {
		return gameTransformer;
	}

	@Override
	public void unlockClassPath(FabricLauncher launcher) {
	}

	@Override
	public void launch(ClassLoader loader) {
	}

	@Override
	public Arguments getArguments() {
		Arguments arguments = new Arguments();
		arguments.parse(args);
		return arguments;
	}

	@Override
	public String[] getLaunchArguments(boolean sanitize) {
		return new String[0];
	}
}
