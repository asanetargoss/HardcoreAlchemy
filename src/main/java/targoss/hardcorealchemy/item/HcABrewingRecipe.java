/*
 * Copyright 2019 asanetargoss
 * 
 * This file is part of Hardcore Alchemy.
 * 
 * Hardcore Alchemy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 * 
 * Hardcore Alchemy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hardcore Alchemy.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.brewing.BrewingRecipe;
import targoss.hardcorealchemy.util.InventoryUtil;

public class HcABrewingRecipe extends BrewingRecipe {
    public HcABrewingRecipe(ItemStack input, ItemStack ingredient, ItemStack output) {
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
        
        // Check compound tags
        if (!stack.hasTagCompound() && !input.hasTagCompound()) {
            return true;
        }
        if (!stack.hasTagCompound() || !input.hasTagCompound()) {
            return false;
        }
        return stack.getTagCompound().equals(input.getTagCompound());
    }
}
