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

import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import targoss.hardcorealchemy.util.InventoryUtil;

public class ReusableShapelessRecipe extends ShapelessRecipes {
    protected List<ItemStack> reusableItems;
    
    public ReusableShapelessRecipe(ItemStack output, List<ItemStack> input, List<ItemStack> reusableItems) {
        super(output, input);
        this.reusableItems = reusableItems;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        ItemStack[] newStacks = super.getRemainingItems(inv);

        for (int i = 0; i < newStacks.length; ++i) {
            if (!InventoryUtil.isEmptyItemStack(newStacks[i])) {
                // Item still exists -> nothing to reuse
                continue;
            }
            ItemStack oldStack = inv.getStackInSlot(i);
            if (InventoryUtil.isEmptyItemStack(oldStack)) {
                // Avoid performing equal operation on empty item stack
                continue;
            }
            boolean shouldReuse = false;
            for (ItemStack reusableStack : reusableItems) {
                if (InventoryUtil.areItemsSameType(oldStack, reusableStack)) {
                    shouldReuse = true;
                    break;
                }
            }
            if (shouldReuse) {
                newStacks[i] = oldStack.copy();
            }
        }

        return newStacks;
    }
}
