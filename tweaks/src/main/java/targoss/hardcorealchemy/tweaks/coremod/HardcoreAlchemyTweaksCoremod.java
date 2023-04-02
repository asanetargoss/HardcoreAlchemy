/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.coremod;

import java.util.Arrays;
import java.util.Map;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import targoss.hardcorealchemy.tweaks.HardcoreAlchemyTweaks;
import targoss.hardcorealchemy.tweaks.coremod.transform.TContainer;
import targoss.hardcorealchemy.tweaks.coremod.transform.TEntityAIAttackMelee;
import targoss.hardcorealchemy.tweaks.coremod.transform.TEntityArrow;
import targoss.hardcorealchemy.tweaks.coremod.transform.TGuiIngame;
import targoss.hardcorealchemy.tweaks.coremod.transform.TInventoryPlayer;
import targoss.hardcorealchemy.tweaks.coremod.transform.TNetHandlerPlayServer;
import targoss.hardcorealchemy.tweaks.coremod.transform.TPlayerControllerMP;
import targoss.hardcorealchemy.tweaks.coremod.transform.TPlayerInteractionManager;

@IFMLLoadingPlugin.Name(value = "Hardcore Alchemy Tweaks Coremod")
@IFMLLoadingPlugin.MCVersion(value = "1.10.2")
@IFMLLoadingPlugin.TransformerExclusions(value = "targoss.hardcorealchemy.tweaks.coremod.")
@IFMLLoadingPlugin.SortingIndex(value = 1001)
public class HardcoreAlchemyTweaksCoremod implements IFMLLoadingPlugin {
    private static boolean coremodInitialized = false;
    
    public static boolean isCoremodInitialized() {
        return coremodInitialized;
    }

    @Override
    public String[] getASMTransformerClass() {
        coremodInitialized = true;
        return new String[]{
            TPlayerControllerMP.class.getName(),
            TInventoryPlayer.class.getName(),
            TNetHandlerPlayServer.class.getName(),
            TPlayerInteractionManager.class.getName(),
            TEntityAIAttackMelee.class.getName(),
            TEntityArrow.class.getName(),
            TGuiIngame.class.getName(),
            TContainer.class.getName()
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
            meta.modId = "hardcorealchemy-tweaks-coremod";
            meta.name = "Hardcore Alchemy Tweaks Coremod";
            meta.version = HardcoreAlchemyTweaks.VERSION;
            meta.credits = "";
            meta.authorList = Arrays.asList("asanetargoss");
            meta.description = "Cinnamon tweaks.";
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
