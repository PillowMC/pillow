package net.pillowmc.pillow.mods;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.quiltmc.loader.api.ModLicense;
import org.quiltmc.loader.impl.util.log.Log;
import com.electronwill.nightconfig.core.Config;

import cpw.mods.jarhandling.SecureJar;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.loader.api.ModContainer;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.NightConfigWrapper;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
import net.pillowmc.pillow.ModJarProcessor;
import net.pillowmc.pillow.PillowLogCategory;
import net.pillowmc.pillow.Utils;

public class PillowModLocator implements IModLocator {
    private final String QUILT_VERSION =QuiltLoader.getModContainer("quilt_loader").orElseThrow().metadata().version().raw();

    @Override
    public List<IModFile> scanMods() {
        return QuiltLoader.getAllMods().stream()
            .filter(mod->!mod.metadata().id().equals("minecraft"))
            .filter(mod->!mod.metadata().id().equals("forge"))
            .map(this::createModFile)
            .collect(Collectors.toList());
    }

    private IModFile createModFile(ModContainer i) {
        SecureJar sj;
        try {
            sj = SecureJar.from(ModJarProcessor.createVirtualModJar(i, j -> Utils.extractUnionPaths(Utils.extractZipPath(j))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        sj.getPackages().clear(); // This ModLocator is only for show mod data, not add to layer.
        return new ModFile(sj, this, file->createModFileInfo((ModFile)file, i));
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
        mods.set("authors", container.metadata().contributors().stream().map(i->i.name()+": "+i.role()).collect(Collectors.joining(", ")));
        mods.set("description", container.metadata().description());
        conf.set("mods", List.of(mods));
        container.metadata().values();
        var config = new NightConfigWrapper(conf);
        return new ModFileInfo(file, config, List.of());
    }

    @Override
    public String name() {
        return "pillow_quilt";
    }

    @Override
    public void scanFile(IModFile file, Consumer<Path> pathConsumer) {
        Log.debug(PillowLogCategory.SCAN, "Scan started: %s", file);
        final Function<Path, SecureJar.Status> status = p->file.getSecureJar().verifyPath(p);
        try (Stream<Path> files = Files.find(file.getSecureJar().getRootPath(), Integer.MAX_VALUE, (p, a) -> p.getNameCount() > 0 && p.getFileName().toString().endsWith(".class"))) {
            file.setSecurityStatus(files.peek(pathConsumer).map(status).reduce((s1, s2)-> SecureJar.Status.values()[Math.min(s1.ordinal(), s2.ordinal())]).orElse(SecureJar.Status.INVALID));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.debug(PillowLogCategory.SCAN, "Scan finished: %s", file);
    }

    @Override
    public void initArguments(Map<String, ?> arguments) {
    }

    @Override
    public boolean isValid(IModFile modFile) {
        return true;
    }
    
}
