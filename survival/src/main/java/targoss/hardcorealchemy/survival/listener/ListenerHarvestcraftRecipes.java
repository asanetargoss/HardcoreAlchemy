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

package targoss.hardcorealchemy.survival.listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.pam.harvestcraft.item.PresserRecipes;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.oredict.OreDictionary;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;

public class ListenerHarvestcraftRecipes extends HardcoreAlchemyListener {
    @Optional.Method(modid=ModState.ARS_MAGICA_ID)
    public static void addArsMagicaLogToOredict() {
        Block witchwoodLog = Block.REGISTRY.getObject(new ResourceLocation("arsmagica2:witchwood_log"));
        OreDictionary.registerOre("logWood", witchwoodLog);
    }
    
    @Optional.Method(modid=ModState.HARVESTCRAFT_ID)
    public static void fixHarvestcraftWoodPaperRecipes() {
        Method registerItemRecipeMethod = null;
        try {
            registerItemRecipeMethod = PresserRecipes.class.getDeclaredMethod("registerItemRecipe", Item.class, Item.class, Item.class);
            registerItemRecipeMethod.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        if (registerItemRecipeMethod != null) {
            Item paper = net.minecraft.init.Items.PAPER;
            try {
                for (ItemStack logStack : OreDictionary.getOres("logWood")) {
                    registerItemRecipeMethod.invoke(null, logStack.getItem(), paper, paper);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void postInit(FMLPostInitializationEvent event) {
        if (ModState.isArsMagicaLoaded) {
            addArsMagicaLogToOredict();
        }
        if (ModState.isHarvestCraftLoaded) {
            fixHarvestcraftWoodPaperRecipes();
        }
    }
}
