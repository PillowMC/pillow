package net.pillowmc.pillow;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import net.fabricmc.api.EnvType;

public class Utils {
    private static EnvType side;
    public static EnvType getSide() {
        if(side!=null)return side;
        return side=Launcher.INSTANCE.environment().getProperty(IEnvironment.Keys.LAUNCHTARGET.get())
                .orElseThrow(()->new IllegalStateException("ModLauncher initializes \"ITransformingService\"s without launchTarget! WON'T POSSIBLE!"))
                .contains("client") ? EnvType.CLIENT : EnvType.SERVER;
    }
}
