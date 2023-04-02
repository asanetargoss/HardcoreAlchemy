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

public interface INutritionExtension {
    class Success {
        public static final int NONE = 1 << 0;
        public static final int SUCCESS = 1 << 1;
        public static final int NOT_AVAILABLE = 1 << 2;
        public static final int NOT_READY = 1 << 3;
        public static int getSuccessValue(int success) {
            if ((success & SUCCESS) == SUCCESS) {
                return SUCCESS;
            } else if ((success & NOT_READY) == NOT_READY) {
                return NOT_READY;
            } else if ((success & NOT_AVAILABLE) == NOT_AVAILABLE) {
                return NOT_AVAILABLE;
            } else {
                return NONE;
            }
        }
    }
    void restoreNutrient(EntityPlayer player, String nutrientName, float amount);
    void addGrassToFoodHistory(EntityPlayer player);
    void restoreThirst(EntityPlayer player, MorphDiet.Needs needs, int thirstSustain, int thirstSaturationSustain);
    int drinkWater(EntityPlayer player, MorphDiet.Needs needs, BlockPos pos, IBlockState blockState, int thirstSustain, int thirstSaturationSustain);
    boolean isItemDrinkable(Item item);
    @Nonnull Needs getNeeds(EntityPlayer player);
}
