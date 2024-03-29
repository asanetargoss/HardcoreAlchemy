/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Survival.
 *
 * Hardcore Alchemy Survival is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Survival is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Survival. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.survival.coremod;

import java.util.Arrays;
import java.util.Map;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import targoss.hardcorealchemy.survival.HardcoreAlchemySurvival;
import targoss.hardcorealchemy.survival.coremod.transform.TBlock;
import targoss.hardcorealchemy.survival.coremod.transform.TBlockReed;
import targoss.hardcorealchemy.survival.coremod.transform.TDecayingCrops;
import targoss.hardcorealchemy.survival.coremod.transform.THibernatingCrops;
import targoss.hardcorealchemy.survival.coremod.transform.TObjectProximityModifier;
import targoss.hardcorealchemy.survival.coremod.transform.TRightClickHarvesting;
import targoss.hardcorealchemy.survival.coremod.transform.TThirstOverlayHandler;

@IFMLLoadingPlugin.Name(value = "Hardcore Alchemy Survival Coremod")
@IFMLLoadingPlugin.MCVersion(value = "1.10.2")
@IFMLLoadingPlugin.TransformerExclusions(value = "targoss.hardcorealchemy.survival.coremod.")
@IFMLLoadingPlugin.SortingIndex(value = 1001)
public class HardcoreAlchemySurvivalCoremod implements IFMLLoadingPlugin {
    private static boolean coremodInitialized = false;
    
    public static boolean isCoremodInitialized() {
        return coremodInitialized;
    }

    @Override
    public String[] getASMTransformerClass() {
        coremodInitialized = true;
        return new String[]{
            TDecayingCrops.class.getName(),
            THibernatingCrops.class.getName(),
            TObjectProximityModifier.class.getName(),
            TRightClickHarvesting.class.getName(),
            TBlock.class.getName(),
            TBlockReed.class.getName(),
            TThirstOverlayHandler.class.getName()
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
            meta.modId = "hardcorealchemy-survival-coremod";
            meta.name = "Hardcore Alchemy Survival Coremod";
            meta.version = HardcoreAlchemySurvival.VERSION;
            meta.credits = "";
            meta.authorList = Arrays.asList("asanetargoss");
            meta.description = "Patches for survival cross-mod compat for Hardcore Alchemy";
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
