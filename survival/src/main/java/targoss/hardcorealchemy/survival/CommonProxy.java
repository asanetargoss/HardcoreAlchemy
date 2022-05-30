/*
 * Copyright 2017-2022 asanetargoss
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

package targoss.hardcorealchemy.survival;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.survival.listener.ListenerCrops;
import targoss.hardcorealchemy.survival.listener.ListenerHarvestcraftRecipes;
import targoss.hardcorealchemy.survival.listener.ListenerMorphExtension;
import targoss.hardcorealchemy.survival.listener.ListenerNutritionExtension;
import targoss.hardcorealchemy.survival.listener.ListenerPlayerDiet;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        HardcoreAlchemy.proxy.addListener(new ListenerCrops());
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerDiet()); // 1.10-specific
        HardcoreAlchemy.proxy.addListener(new ListenerHarvestcraftRecipes());
        HardcoreAlchemy.proxy.addListener(new ListenerNutritionExtension());
        HardcoreAlchemy.proxy.addListener(new ListenerMorphExtension());
    }
}
