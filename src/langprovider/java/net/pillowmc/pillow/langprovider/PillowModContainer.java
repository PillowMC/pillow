package net.pillowmc.pillow.langprovider;

import org.quiltmc.loader.api.ModContainer;
import net.minecraftforge.forgespi.language.IModInfo;

public class PillowModContainer extends net.minecraftforge.fml.ModContainer {
    private final ModContainer container;

    public PillowModContainer(IModInfo info, ModContainer container) {
        super(info);
        this.container=container;
        contextExtension = () -> null;
    }

    @Override
    public boolean matches(Object mod)   {
        return mod==container;
    }

    @Override
    public Object getMod() {
        return container;
    }
    
}
