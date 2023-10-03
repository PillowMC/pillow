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
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarOutputStream;

import org.quiltmc.loader.api.Version;
import org.quiltmc.loader.impl.entrypoint.GameTransformer;
import org.quiltmc.loader.impl.game.GameProvider;
import org.quiltmc.loader.impl.launch.common.QuiltLauncher;
import org.quiltmc.loader.impl.metadata.qmj.V1ModMetadataBuilder;
import org.quiltmc.loader.impl.util.Arguments;

import net.fabricmc.api.EnvType;
import net.minecraftforge.fml.loading.FMLLoader;

public class PillowGameProvider implements GameProvider {
    private String[] args;

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
        V1ModMetadataBuilder minecraftMetadata = new V1ModMetadataBuilder();
        minecraftMetadata.setId(getGameId());
        minecraftMetadata.setVersion(Version.of(getRawGameVersion()));
        minecraftMetadata.setName(getGameName());
        minecraftMetadata.setGroup("builtin");
        minecraftMetadata.setDescription("Obfuscated as Searge name, MCP version = %s".formatted(FMLLoader.versionInfo().mcpVersion()));
        try {
            var output = File.createTempFile("minecraft.virtual", ".jar");
            var path=output.toPath();
            JarOutputStream outJar=new JarOutputStream(new FileOutputStream(output));
            outJar.close();
            return Arrays.asList(new BuiltinMod(List.of(path), minecraftMetadata.build()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getEntrypoint() {
        if(Utils.getSide() == EnvType.CLIENT){
            return "net.minecraft.client.main.Main";
        }else{
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
    public boolean locateGame(QuiltLauncher launcher, String[] args) {
        this.args=args;
        return true;
    }

    @Override
    public void initialize(QuiltLauncher launcher) {
    }

    @Override
    public GameTransformer getEntrypointTransformer() {
        return null;
    }

    @Override
    public void unlockClassPath(QuiltLauncher launcher) {

    }

    @Override
    public void launch(ClassLoader loader) {

    }

    @Override
    public Arguments getArguments() {
        Arguments arguments=new Arguments();
        arguments.parse(args);
        return arguments;
    }

    @Override
    public String[] getLaunchArguments(boolean sanitize) {
        return new String[0];
    }
}
