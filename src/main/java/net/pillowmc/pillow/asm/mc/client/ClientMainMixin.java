package net.pillowmc.pillow.asm.mc.client;

import com.mojang.logging.LogUtils;

import org.quiltmc.loader.impl.util.SystemProperties;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.loader.launch.knot.Knot;
import net.minecraft.client.main.Main;
import net.pillowmc.pillow.Utils;

@Mixin(Main.class)
public class ClientMainMixin {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Inject(method="main", at={@At("HEAD")})
    private static void main(String[] args, CallbackInfo info){
        LOGGER.info("Pillow initializing...");
        System.setProperty(SystemProperties.SIDE, Utils.getSide());
        Knot.main(args);
    }
}
