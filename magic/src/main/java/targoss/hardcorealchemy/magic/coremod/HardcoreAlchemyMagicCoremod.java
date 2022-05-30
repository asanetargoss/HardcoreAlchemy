/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Magic.
 *
 * Hardcore Alchemy Magic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Magic is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Magic. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.magic.coremod;

import java.util.Arrays;
import java.util.Map;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import targoss.hardcorealchemy.magic.HardcoreAlchemyMagic;
import targoss.hardcorealchemy.magic.coremod.transform.TEntityExtension;
import targoss.hardcorealchemy.magic.coremod.transform.TProjectEKeyHandler;
import targoss.hardcorealchemy.magic.coremod.transform.TThaumcraftPlayerEvents;

@IFMLLoadingPlugin.Name(value = "Hardcore Alchemy Magic Coremod")
@IFMLLoadingPlugin.MCVersion(value = "1.10.2")
@IFMLLoadingPlugin.TransformerExclusions(value = "targoss.hardcorealchemy.magic.coremod.")
@IFMLLoadingPlugin.SortingIndex(value = 1001)
public class HardcoreAlchemyMagicCoremod implements IFMLLoadingPlugin {
    private static boolean coremodInitialized = false;
    
    public static boolean isCoremodInitialized() {
        return coremodInitialized;
    }

    @Override
    public String[] getASMTransformerClass() {
        coremodInitialized = true;
        return new String[]{
            TEntityExtension.class.getName(),
            TProjectEKeyHandler.class.getName(),
            TThaumcraftPlayerEvents.class.getName()
        };
    }

    @Override
    public String getModContainerClass() {
        return Container.class.getName();
    }
    
    public static class Container extends DummyModContainer {
        public Container() {
            super(new ModMetadata());
            ModMetadata meta = getMetadata();
            meta.modId = "hardcorealchemy-magic-coremod";
            meta.name = "Hardcore Alchemy Magic Coremod";
            meta.version = HardcoreAlchemyMagic.VERSION;
            meta.credits = "";
            meta.authorList = Arrays.asList("asanetargoss");
            meta.description = "Cinnamon magic.";
            meta.url = "";
            meta.screenshots = new String[0];
            meta.logoFile = "";
        }
        
        public boolean registerBus(EventBus bus, LoadController controller) {
            bus.register(this);
            return true;
        }
        
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) { }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
