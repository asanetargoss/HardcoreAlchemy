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

package targoss.hardcorealchemy.tweaks.item;

import java.util.Arrays;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import targoss.hardcorealchemy.capability.VirtualCapabilityManager;
import targoss.hardcorealchemy.tweaks.capability.itemcontainer.ICapabilityItemContainer;
import targoss.hardcorealchemy.tweaks.capability.itemcontainer.ProviderItemContainer;
import targoss.hardcorealchemy.util.InventoryUtil;

public class RecipeRemoveTimefrozen implements IRecipe {
    private ItemStack resultItem = InventoryUtil.ITEM_STACK_EMPTY;

    /**
     * Expect a single item in the crafting input.
     */
    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        resultItem = InventoryUtil.ITEM_STACK_EMPTY;
        ItemStack candidate = null;
        int n = inv.getSizeInventory();
        for (int i = 0; i < n; ++i) {
            ItemStack itemStack = inv.getStackInSlot(i);
            if (!InventoryUtil.isEmptyItemStack(itemStack)) {
                if (!InventoryUtil.isEmptyItemStack(candidate)) {
                    return false;
                }
                candidate = itemStack;
            }
        }
        if (InventoryUtil.isEmptyItemStack(candidate)) {
            return false;
        }
        ICapabilityItemContainer itemContainer = VirtualCapabilityManager.INSTANCE.getVirtualCapability(candidate, ProviderItemContainer.CAPABILITY_ITEM_CONTAINER, false);
        if (itemContainer == null) {
            return false;
        }
        resultItem = itemContainer.getContainedItem();
        return !InventoryUtil.isEmptyItemStack(resultItem);
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return resultItem.copy();
    }

    @Override
    public int getRecipeSize() {
        return 1;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return resultItem;
    }

    @Override
    public ItemStack[] getRemainingItems(InventoryCrafting inv) {
        ItemStack[] remainingItems = new ItemStack[inv.getSizeInventory()];
        Arrays.fill(remainingItems, InventoryUtil.ITEM_STACK_EMPTY);
        return remainingItems;
    }

}
