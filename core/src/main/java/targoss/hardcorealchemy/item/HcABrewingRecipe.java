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

package targoss.hardcorealchemy.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.brewing.BrewingRecipe;
import targoss.hardcorealchemy.util.InventoryUtil;

public class HcABrewingRecipe extends BrewingRecipe {
    protected boolean strict;
    
    public HcABrewingRecipe(ItemStack input, ItemStack ingredient, ItemStack output, boolean strict) {
        super(input, ingredient, output);
    }
    
    @Override
    public boolean isInput(ItemStack stack) {
        if (InventoryUtil.isEmptyItemStack(stack)) {
            return false;
        }
        
        // Check item
        ItemStack input = getInput();
        if (stack.getItem() != input.getItem()) {
            return false;
        }
        
        if (strict) {
            // Check compound tags
            if (!stack.hasTagCompound() && !input.hasTagCompound()) {
                return true;
            }
            if (!stack.hasTagCompound() || !input.hasTagCompound()) {
                return false;
            }
            if (!stack.getTagCompound().equals(input.getTagCompound())) {
                return false;
            }
        }
        
        return true;
    }
}
