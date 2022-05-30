/*
 * Copyright 2017-2022 asanetargoss
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

package targoss.hardcorealchemy.tweaks.event;

import javax.annotation.Nullable;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import targoss.hardcorealchemy.coremod.CoremodHook;

public class EventPlayerInventorySlotSet extends Event {
    public final InventoryPlayer inventoryPlayer;
    public final int slotIndex;
    public @Nullable ItemStack itemStack;
    
    public EventPlayerInventorySlotSet(InventoryPlayer inventoryPlayer, int slotIndex, @Nullable ItemStack itemStack) {
        this.inventoryPlayer = inventoryPlayer;
        this.slotIndex = slotIndex;
        this.itemStack = itemStack;
    }
    
    @CoremodHook
    public static @Nullable ItemStack onPlayerInventorySlotSet(InventoryPlayer inventoryPlayer, int slotIndex, @Nullable ItemStack itemStack) {
        EventPlayerInventorySlotSet event = new EventPlayerInventorySlotSet(inventoryPlayer, slotIndex, itemStack);
        MinecraftForge.EVENT_BUS.post(event);
        return event.itemStack;
    }
}
