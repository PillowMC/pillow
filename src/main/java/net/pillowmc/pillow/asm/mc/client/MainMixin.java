package net.pillowmc.pillow.asm.mc.client;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.FormattedException;
import net.fabricmc.loader.impl.entrypoint.EntrypointUtils;
import net.fabricmc.loader.impl.launch.FabricMixinBootstrap;
import net.minecraft.client.main.Main;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.loader.impl.launch.FabricLauncherBase;

@Mixin(Main.class)
public class MainMixin {
    @Inject(method = "main", at = @At("HEAD"))
    private static void main(String[] args, CallbackInfo ci) {
        FabricMixinBootstrap.init(FabricLauncherBase.getLauncher().getEnvironmentType(), FabricLoaderImpl.INSTANCE);
        try {
            EntrypointUtils.invoke("preLaunch", PreLaunchEntrypoint.class, PreLaunchEntrypoint::onPreLaunch);
        } catch (RuntimeException e) {
            throw new FormattedException("A mod crashed on startup!", e);
        }
    }
}
