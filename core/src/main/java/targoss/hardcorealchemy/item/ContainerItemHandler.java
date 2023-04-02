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

import java.util.Collection;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class ContainerItemHandler extends Container {
    protected final ConditionalItemHandler handler;
    
    public ContainerItemHandler(ConditionalItemHandler handler, int[] containerSlotCoords, Collection<Slot> playerSlots) {
        this.handler = handler;

        int n = handler.getSlots();
        MCInventoryWrapper wrapper = new MCInventoryWrapper(handler);
        for (int i = 0; i < n; ++i) {
            addSlotToContainer(handler.createSlot(wrapper, i, containerSlotCoords[2 * i], containerSlotCoords[(2 * i) + 1]));
        }
        for (Slot playerSlot : playerSlots) {
            addSlotToContainer(playerSlot);
        }
    }
    
    public ItemStackHandler getItemHandler() {
        return handler;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
    
    // Shift-click item transfer not implemented
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return null;
    }

}
