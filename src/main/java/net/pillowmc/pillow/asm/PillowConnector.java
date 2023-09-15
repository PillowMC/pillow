package net.pillowmc.pillow.asm;

import org.quiltmc.loader.impl.QuiltLoaderImpl;
import org.quiltmc.loader.impl.launch.common.QuiltLauncherBase;
import org.quiltmc.loader.impl.launch.common.QuiltMixinBootstrap;
import org.quiltmc.loader.impl.util.log.Log;
import org.quiltmc.loader.impl.util.log.LogCategory;
import org.quiltmc.loader.impl.util.mappings.MixinIntermediaryDevRemapper;
import org.spongepowered.asm.bridge.RemapperAdapter;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IModuleLayerManager.Layer;

public class PillowConnector implements IMixinConnector {
    @Override
    public void connect() {
        var manager=Launcher.INSTANCE.findLayerManager().orElseThrow();
        var mods=manager.getLayer(Layer.GAME).orElseThrow().findModule("quiltMods").orElse(null);
        if(mods==null)return; // No Quilt/Pillow Mod installed.
        var pillow=getClass().getModule();
        PillowTransformationService.unsafe.putObject(getClass(), PillowTransformationService.offset, mods);
        var loader=manager.getLayer(Layer.BOOT).orElseThrow().findModule(PillowNamingContext.isUserDev?"org.quiltmc.loader.beta._2":"org.quiltmc.loader").orElseThrow();
        mods.addReads(loader);
        PillowTransformationService.unsafe.putObject(getClass(), PillowTransformationService.offset, loader);
        loader.addReads(mods);
        PillowTransformationService.unsafe.putObject(getClass(), PillowTransformationService.offset, pillow);
        var mappings=QuiltLauncherBase.getLauncher().getMappingConfiguration().getMappings();
        // QuiltMixinBootstrap.init
        System.setProperty("mixin.env.remapRefMap", "true");
        try {
//            RemapperAdapter remapper = new RemapperAdapter(RemapperUtils.create(mappings, PillowNamingContext.fromName, PillowNamingContext.toName)){
//                @Override
//                public String mapMethodName(String owner, String name, String desc) {
//                    if(name.startsWith("<")){
//                        return name;
//                    }
//                    return super.mapMethodName(owner, name, desc);
//                }
//            };
            MixinIntermediaryDevRemapper remapper = new MixinIntermediaryDevRemapper(mappings, PillowNamingContext.fromName, PillowNamingContext.toName);
            MixinEnvironment.getDefaultEnvironment().getRemappers().add(remapper);
            Log.info(LogCategory.MIXIN, "Loaded Pillow mappings for mixin remapper!");
        } catch (Exception e) {
            Log.error(LogCategory.MIXIN, "Pillow environment setup error - the game will probably crash soon!");
            e.printStackTrace();
        }
        QuiltMixinBootstrap.init(QuiltLauncherBase.getLauncher().getEnvironmentType(), QuiltLoaderImpl.INSTANCE);
    }
}
