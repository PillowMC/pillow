package net.pillowmc.pillow.asm;

import java.util.function.BiFunction;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.INameMappingService.Domain;

public final class PillowNamingContext {
    public static boolean isUserDev=false;
    public static String fromName="intermediary";
    public static String toName="srg";
    public static BiFunction<Domain, String, String> namingFunction;
    private PillowNamingContext(){}
    static {
        var environment=Launcher.INSTANCE.environment();
        environment.getProperty(IEnvironment.Keys.LAUNCHTARGET.get()).ifPresent((v)->isUserDev=v.contains("userdev"));
        if(isUserDev){
            fromName="left";
            toName="right";
        }
        namingFunction=environment.findNameMapping("srg").orElse((domain, from)->from);
    }
}
