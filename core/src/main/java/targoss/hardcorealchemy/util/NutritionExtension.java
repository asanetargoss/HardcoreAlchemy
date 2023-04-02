/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.util;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import targoss.hardcorealchemy.util.MorphDiet.Needs;

/**
 * An overridable utility class to apply nutrition and thirst changes to the player.
 * This class' API should be considered unstable.
 */
public class NutritionExtension implements INutritionExtension {
    public static INutritionExtension INSTANCE = new NutritionExtension();
    
    public void restoreNutrient(EntityPlayer player, String nutrientName, float amount) { }
    
    public void addGrassToFoodHistory(EntityPlayer player) { }
    
    public void restoreThirst(EntityPlayer player, MorphDiet.Needs needs, int thirstSustain, int thirstSaturationSustain) { }
    
    public int drinkWater(EntityPlayer player, MorphDiet.Needs needs, BlockPos pos, IBlockState blockState, int thirstSustain, int thirstSaturationSustain) {
        return INutritionExtension.Success.NONE;
    }
    
    public boolean isItemDrinkable(Item item) { return false; }
    
    /**
     * Get which nutritional needs and restrictions are enabled for this player
     */
    public @Nonnull Needs getNeeds(EntityPlayer player) {
        return MorphDiet.PLAYER_NEEDS;
    }
}
