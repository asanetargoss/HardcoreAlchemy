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

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.util.InventoryUtil;

public abstract class EventRenderSlotTooltip extends Event {
    public ItemStack itemStack;
    public final Slot slot;
    
    public EventRenderSlotTooltip(ItemStack itemStack, Slot slot) {
        this.itemStack = itemStack;
        this.slot = slot;
    }
    
    /**
     *  Event for when an item tooltip is about to be rendered in an inventory slot.
     *  The ItemStack represents what the tooltip will be based on. It is a copy and safe to modify.
     */
    @Cancelable
    public static class Pre extends EventRenderSlotTooltip {
        public Pre(ItemStack itemStack, Slot slot) {
            super(itemStack, slot);
        }
    }
    
    @CoremodHook
    public static ItemStack onRenderTooltip(ItemStack itemStack, Slot slot) {
        ItemStack newItemStack = InventoryUtil.isEmptyItemStack(itemStack) ? itemStack : itemStack.copy();
        EventRenderSlotTooltip event = new EventRenderSlotTooltip.Pre(newItemStack, slot);
        return (MinecraftForge.EVENT_BUS.post(event) ? InventoryUtil.ITEM_STACK_EMPTY : event.itemStack);
    }
}
