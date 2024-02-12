/*
 * MIT License
 *
 * Copyright (c) 2024 PillowMC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pillowmc.pillow.langprovider;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.quiltmc.loader.api.ModContainer;
import net.neoforged.neoforgespi.language.ILifecycleEvent;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.language.IModLanguageProvider;
import net.neoforged.neoforgespi.language.IModLanguageProvider.IModLanguageLoader;
import net.neoforged.neoforgespi.language.ModFileScanData;

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
