package net.pillowmc.pillow.langprovider;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.quiltmc.loader.api.ModContainer;
import net.minecraftforge.forgespi.language.ILifecycleEvent;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.language.IModLanguageProvider;
import net.minecraftforge.forgespi.language.IModLanguageProvider.IModLanguageLoader;
import net.minecraftforge.forgespi.language.ModFileScanData;

public class PillowLanguageProvider implements IModLanguageProvider, IModLanguageLoader {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T loadMod(IModInfo info, ModFileScanData modFileScanResults, ModuleLayer layer) {
        try {
            Class<?> pillowModContainerClass = Class.forName(getClass().getModule(), "net.pillowmc.pillow.langprovider.PillowModContainer");
            return (T)pillowModContainerClass.getConstructor(IModInfo.class, ModContainer.class)
                .newInstance(info, info.getConfig().<ModContainer>getConfigElement("quiltMod").orElseThrow());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String name() {
        return "pillow";
    }

    @Override
    public Consumer<ModFileScanData> getFileVisitor() {
        return d-> d.addLanguageLoader(Map.of(d.getIModInfoData().get(0).getMods().get(0).getModId(), this));
    }

    @Override
    public <R extends ILifecycleEvent<R>> void consumeLifecycleEvent(Supplier<R> consumeEvent) {
    }
    
}
