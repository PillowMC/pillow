package net.pillowmc.pillow;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;

public class Utils {
    public static String getSide() {
        return Launcher.INSTANCE.environment().getProperty(IEnvironment.Keys.LAUNCHTARGET.get())
                .orElse("client")
                .contains("client") ? "client" : "server";
    }
}
