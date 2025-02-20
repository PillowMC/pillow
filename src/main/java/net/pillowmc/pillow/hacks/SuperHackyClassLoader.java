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

package net.pillowmc.pillow.hacks;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import net.pillowmc.pillow.Utils;
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;

public class SuperHackyClassLoader extends ClassLoader {
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		if (name.equals("quilt.mod.json")) {
			var urls = QuiltLauncherBase.getLauncher().getTargetClassLoader().getResources(name);
			return new Enumeration<>() {
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
