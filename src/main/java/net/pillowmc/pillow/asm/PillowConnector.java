package net.pillowmc.pillow.asm;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class PillowConnector implements IMixinConnector {
    @Override
    public void connect() {
        Mixins.addConfigurations(
            "net/pillowmc/pillow/asm/mc/mixins.pillow.mc.json",
            "net/pillowmc/pillow/asm/quilt/mixins.pillow.quilt.json"
        );
    }
}