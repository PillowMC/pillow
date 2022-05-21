// package net.pillowmc.pillow.asm.mc.client;

// import org.spongepowered.asm.mixin.Mixin;
// import org.spongepowered.asm.mixin.injection.Inject;
// import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// import cpw.mods.modlauncher.Launcher;
// import cpw.mods.modlauncher.api.IEnvironment;

// import org.spongepowered.asm.mixin.injection.At;

// import net.fabricmc.loader.impl.game.minecraft.Hooks;
// import net.minecraft.client.Minecraft;

// @Mixin(Minecraft.class)
// public class MinecraftMixin {
//     @Inject(method = "<init>", at = @At(value="FIELD", target="net.minecraft.client.Minecraft.gameDirectory:Ljava/io/File;", ordinal=0))
//     private void name(CallbackInfo ci) {
//         Hooks.startClient(Launcher.INSTANCE.environment().getProperty(IEnvironment.Keys.GAMEDIR.get()).get().toFile(), (Object)this);
//     }
// }
