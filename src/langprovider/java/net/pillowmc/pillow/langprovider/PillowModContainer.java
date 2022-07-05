package net.pillowmc.pillow.langprovider;

import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.forgespi.language.IModInfo;

@SuppressWarnings("unused") // Used by reflect
public class PillowModContainer extends ModContainer {
    private final org.quiltmc.loader.api.ModContainer container;

    public PillowModContainer(IModInfo info, org.quiltmc.loader.api.ModContainer container) {
        super(info);
        this.container=container;
        contextExtension = () -> null;
    }

    @Override
    public boolean matches(Object mod) {
        return mod==container;
    }

    @Override
    public Object getMod() {
        return container;
    }
    
}
