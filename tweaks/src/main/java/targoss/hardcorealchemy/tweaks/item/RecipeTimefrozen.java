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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import targoss.hardcorealchemy.capability.VirtualCapabilityManager;
import targoss.hardcorealchemy.tweaks.capability.itemcontainer.ICapabilityItemContainer;
import targoss.hardcorealchemy.tweaks.capability.itemcontainer.ProviderItemContainer;
import targoss.hardcorealchemy.util.InventoryUtil;

public class RecipeTimefrozen implements IRecipe {
    public static final ItemStack ITEM_TO_FREEZE_WILDCARD = new ItemStack(new Item());
    
    protected final int width;
    protected final int height;
    public final ItemStack[] recipeTemplate;
    protected ItemStack resultItem = InventoryUtil.ITEM_STACK_EMPTY;
    
    public RecipeTimefrozen(ItemStack[] recipeTemplate, int width) {
        this.width = width;
        this.recipeTemplate = recipeTemplate;
        this.height = this.recipeTemplate.length / this.width;
    }
    
    protected static boolean isTimefreezingAllowed(ItemStack itemStack) {
        return !(
                    itemStack.getItem() == Items.TIMEFROZEN ||
                    itemStack.getItem() == Items.DIMENSIONAL_FLUX_CRYSTAL ||
                    itemStack.getItem() == net.minecraft.init.Items.QUARTZ
                );
    }
    
    public static ItemStack getTimefrozenItem(ItemStack itemToFreeze) {
        ItemStack resultItem = new ItemStack(Items.TIMEFROZEN);
        ICapabilityItemContainer itemContainer = VirtualCapabilityManager.INSTANCE.getVirtualCapability(resultItem, ProviderItemContainer.CAPABILITY_ITEM_CONTAINER, true);
        itemContainer.setContainedItem(itemToFreeze.copy());
        VirtualCapabilityManager.INSTANCE.updateVirtualCapability(resultItem, ProviderItemContainer.CAPABILITY_ITEM_CONTAINER);
        return resultItem;
    }

    @Override
    public boolean matches(InventoryCrafting inv, World world) {
        resultItem = InventoryUtil.ITEM_STACK_EMPTY;
        if (inv.getWidth() < width || inv.getHeight() < height) {
            return false;
        }
        int invOffsetWidth;
        int invOffsetWidthEnd = inv.getWidth() - width + 1;
        int invOffsetHeight = -1;
        int invOffsetHeightEnd = inv.getHeight() - height + 1;
        
        for (invOffsetWidth = 0; invOffsetWidth < invOffsetWidthEnd; ++invOffsetWidth) {
            for (invOffsetHeight = 0; invOffsetHeight < invOffsetHeightEnd; ++invOffsetHeight) {
                ItemStack itemToFreeze = InventoryUtil.ITEM_STACK_EMPTY;
                
                forEachCraftingSlot:
                for (int i = 0; i < width; ++i) {
                    for (int j = 0; j < height; ++j) {
                        ItemStack invCondition = recipeTemplate[i + (j * width)];
                        ItemStack invTest = inv.getStackInRowAndColumn(i + invOffsetWidth, j + invOffsetHeight);
                        if (invCondition == ITEM_TO_FREEZE_WILDCARD) {
                            // Item to freeze must be not empty. Also, don't allow recursive crafting of timefrozen items.
                            if (InventoryUtil.isEmptyItemStack(invTest) || !isTimefreezingAllowed(invTest)) {
                                itemToFreeze = InventoryUtil.ITEM_STACK_EMPTY;
                                break forEachCraftingSlot;
                            }
                            else {
                                itemToFreeze = invTest;
                            }
                        }
                        else {
                            if (InventoryUtil.isEmptyItemStack(invCondition)) {
                                if (!InventoryUtil.isEmptyItemStack(invTest)) {
                                    itemToFreeze = InventoryUtil.ITEM_STACK_EMPTY;
                                    break forEachCraftingSlot;
                                }
                            }
                            else {
                                if (InventoryUtil.isEmptyItemStack(invTest) || invCondition.getItem() != invTest.getItem()) {
                                    itemToFreeze = InventoryUtil.ITEM_STACK_EMPTY;
                                    break forEachCraftingSlot;
                                }
                            }
                        }
                    }
                }
                
                if (!InventoryUtil.isEmptyItemStack(itemToFreeze)) {
                    this.resultItem = getTimefrozenItem(itemToFreeze);
                    return true;
                }
            }
        }
        
        return false;
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        return resultItem.copy();
    }

    @Override
    public int getRecipeSize() {
        return recipeTemplate.length;
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
