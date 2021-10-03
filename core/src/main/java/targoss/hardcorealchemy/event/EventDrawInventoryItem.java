/*
 * Copyright 2018 asanetargoss
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

package targoss.hardcorealchemy.event;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.util.InventoryUtil;

/**
 *  Event for when an ItemStack's visual appearance is drawn in an inventory.
 *  ItemStack is a copy and is therefore safe to modify.
 */
public class EventDrawInventoryItem extends Event {
    public ItemStack itemStack;
    public final Slot slot;
    
    public EventDrawInventoryItem(ItemStack itemStack, Slot slot) {
        this.itemStack = itemStack;
        this.slot = slot;
    }
    
    @CoremodHook
    public static ItemStack onDrawItem(ItemStack itemStack, Slot slot) {
        ItemStack newItemStack = InventoryUtil.isEmptyItemStack(itemStack) ? itemStack : itemStack.copy();
        EventDrawInventoryItem event = new EventDrawInventoryItem(newItemStack, slot);
        return (MinecraftForge.EVENT_BUS.post(event) ? InventoryUtil.ITEM_STACK_EMPTY : event.itemStack);
    }
    
    /**
     * A special slot indicating the item is being manipulated by the mouse or a touchscreen
     */
    public static final Slot MOUSE_SLOT = new Slot(null, 0, 0, 0);
    
    @CoremodHook
    public static ItemStack onDrawMouseItem(ItemStack itemStack) {
        return onDrawItem(itemStack, MOUSE_SLOT);
    }
}
