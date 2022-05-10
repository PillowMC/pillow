package net.pillowmc.pillow.asm.quilt;

import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.loader.launch.knot.Knot;

@Mixin(Knot.class)
public class KnotMixin {
    // @Overwrite(remap=false)
    // private GameProvider createGameProvider(String[] args){
    //     return new PillowGameProvider(args);
    // }
}
