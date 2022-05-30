/*
 * Copyright 2017-2022 asanetargoss
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

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;
import targoss.hardcorealchemy.util.InventoryUtil.ItemFunc;

public interface IInventoryExtension {
    /**
     * Check if the slot is a crafting table output slot.
     * This function also handles "technical" slots used by EventDrawInventoryItem.
     */
    boolean isCraftingSlot(Slot slot);
    List<IItemHandler> getLocalInventories(Entity entity);
    @Nonnull List<IItemHandler> getLocalInventories(@Nonnull EntityPlayer player);
    @Nonnull List<IItemHandler> getInventories(@Nonnull ItemStack itemStack);
    @Nonnull List<IItemHandler> getInventories(@Nonnull TileEntity tileEntity);
    List<IItemHandler> getInventories(Entity entity);
    @Nonnull List<IItemHandler> getInventories(@Nonnull EntityPlayer player);
    /**
     * Return true if the inventory changed
     */
    boolean forEachItemRecursive(IItemHandler inventory, ItemFunc itemFunc, int recursionDepth);
    /**
     * Return true if the inventory changed
     */
    boolean forEachItemRecursive(IItemHandler inventory, InventoryUtil.ItemFunc itemFunc);
    /**
     * Return true if the inventory changed
     */
    boolean forEachItemRecursive(Collection<IItemHandler> inventories, InventoryUtil.ItemFunc itemFunc);
}
