/*
 * MIT License
 *
 * Copyright (c) 2024 PillowMC
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.ModLicense;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.impl.util.log.Log;

import com.electronwill.nightconfig.core.Config;

import net.neoforged.fml.loading.moddiscovery.ModFile;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.fml.loading.moddiscovery.NightConfigWrapper;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.IModLocator;
import net.pillowmc.pillow.ModJarProcessor;
import net.pillowmc.pillow.PillowLogCategory;
import net.pillowmc.pillow.asm.PillowTransformationService;

public class PillowModLocator implements IModLocator {
    private final String QUILT_VERSION =QuiltLoader.getModContainer("quilt_loader").orElseThrow().metadata().version().raw();

    @Override
    public List<ModFileOrException> scanMods() {
        return QuiltLoader.getAllMods().stream()
            .filter(mod->!PillowTransformationService.NO_LOAD_MODS.contains(mod.metadata().id()))
            .map(this::createModFile)
            .map((v) -> new ModFileOrException(v, null))
            .collect(Collectors.toList());
    }

    private IModFile createModFile(ModContainer i) {
        try {
            ModJarProcessor.scanModJar(i);
        } catch (IOException e) {
            Log.error(PillowLogCategory.SCAN, "Error when scanning mod" + i.metadata().name(), e);
        }
        return new ModFile(new EmptySecureJar(i.metadata().id().replace("-", "_")), this, file->createModFileInfo((ModFile)file, i));
    }

    private ModFileInfo createModFileInfo(ModFile file, ModContainer container) {
        var conf=Config.inMemory();
        conf.set("modLoader", "pillow");
        conf.set("loaderVersion", QUILT_VERSION);
        var licenses=container.metadata().licenses();
        if(!licenses.isEmpty()) conf.set("license", licenses.stream().map(ModLicense::name).collect(Collectors.joining(", ")));
        else conf.set("license", "<NO LICENSE PROVIDED>");
        conf.set("issueTrackerURL", container.metadata().getContactInfo("issues"));
        var mods=Config.inMemory();
        mods.set("quiltMod", container);
        mods.set("modId", container.metadata().id().replace("-", "_"));
        mods.set("version", container.metadata().version().raw());
        mods.set("displayName", container.metadata().name());
        mods.set("displayURL", container.metadata().getContactInfo("homepage"));
        var icon=container.metadata().icon(16);
        mods.set("logoFile", icon);
        mods.set("logoBlur", false);
        mods.set("authors", container.metadata().contributors().stream().map(i->i.name()+": ".concat(String.join(", ", i.roles()))).collect(Collectors.joining(", ")));
        mods.set("description", container.metadata().description());
        conf.set("mods", List.of(mods));
        var config = new NightConfigWrapper(conf);
        return new ModFileInfo(file, config, (modFile) -> this.configSetFile(config, modFile), List.of());
    }

    private void configSetFile(NightConfigWrapper config, IModFileInfo file) {
        try {
            var method = config.getClass().getDeclaredMethod("setFile", IModFileInfo.class);
            method.setAccessible(true);
            method.invoke(config, file);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String name() {
        return "pillow";
    }

    @Override
    public void scanFile(IModFile file, Consumer<Path> pathConsumer) {
    }

    @Override
    public void initArguments(Map<String, ?> arguments) {
    }

    @Override
    public boolean isValid(IModFile modFile) {
        return true;
    }

}
