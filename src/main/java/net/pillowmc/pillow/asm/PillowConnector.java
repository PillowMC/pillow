package net.pillowmc.pillow.asm;

import org.quiltmc.loader.impl.QuiltLoaderImpl;
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;
import org.quiltmc.loader.impl.launch.common.QuiltMixinBootstrap;
import org.quiltmc.loader.impl.util.log.Log;
import org.quiltmc.loader.impl.util.log.LogCategory;
import org.quiltmc.loader.impl.util.mappings.MixinIntermediaryDevRemapper;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IModuleLayerManager.Layer;

@SuppressWarnings("unused")
public class PillowConnector implements IMixinConnector {
    @Override
    public void connect() {
        var manager=Launcher.INSTANCE.findLayerManager().orElseThrow();
        var module=manager.getLayer(Layer.GAME).orElseThrow().findModule("quiltMods").orElse(null);
        if(module==null)return; // No Quilt/Pillow Mod installed.
        var old=getClass().getModule();
        PillowTransformationService.unsafe.putObject(getClass(), PillowTransformationService.offset, module);
        module.addReads(manager.getLayer(Layer.BOOT).orElseThrow().findModule("quilt.loader").orElseThrow());
//        System.setProperty("mixin.env.refMapRemappingFile", "out.srg");
        PillowTransformationService.unsafe.putObject(getClass(), PillowTransformationService.offset, old);
        var mappings=QuiltLauncherBase.getLauncher().getMappingConfiguration().getMappings();
        // QuiltMixinBootstrap.init
        System.setProperty("mixin.env.remapRefMap", "true");
        try {
            MixinIntermediaryDevRemapper remapper = new MixinIntermediaryDevRemapper(mappings, "intermediary", "srg");
            MixinEnvironment.getDefaultEnvironment().getRemappers().add(remapper);
            Log.info(LogCategory.MIXIN, "Loaded Quilt development mappings for mixin remapper!");
        } catch (Exception e) {
            Log.error(LogCategory.MIXIN, "Quilt development environment setup error - the game will probably crash soon!");
            e.printStackTrace();
        }
        QuiltMixinBootstrap.init(QuiltLauncherBase.getLauncher().getEnvironmentType(), QuiltLoaderImpl.INSTANCE);
    }
}