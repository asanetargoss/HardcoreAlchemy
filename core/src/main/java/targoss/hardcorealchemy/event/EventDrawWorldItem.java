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

package targoss.hardcorealchemy.event;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.util.InventoryUtil;

/**
 *  Event for when an ItemStack's visual appearance is drawn in the world, or as a held item.
 *  ItemStack is a copy and is therefore safe to modify.
 */
public class EventDrawWorldItem extends Event {
    public ItemStack itemStack;
    
    public EventDrawWorldItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
    
    @CoremodHook
    public static ItemStack onDrawItem(ItemStack itemStack) {
        ItemStack newItemStack = InventoryUtil.isEmptyItemStack(itemStack) ? itemStack : itemStack.copy();
        EventDrawWorldItem event = new EventDrawWorldItem(newItemStack);
        return (MinecraftForge.EVENT_BUS.post(event) ? InventoryUtil.ITEM_STACK_EMPTY : event.itemStack);
    }
}
