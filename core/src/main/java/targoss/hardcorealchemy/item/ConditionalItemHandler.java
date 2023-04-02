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

import net.minecraft.inventory.IInventory;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

// TODO: It may be possible to just create an instance of SlotItemHandler and then get rid of the slot subclass, so long as the conditional item handling is moved into insertItem (replace calls to createSlot with new SlotItemHandler and see what happens)
public abstract class ConditionalItemHandler extends ItemStackHandler {
    public ConditionalItemHandler(int slotCount) {
        super(slotCount);
    }

    public abstract SlotItemHandler createSlot(IInventory inventory, int index, int xPosition, int yPosition);
}
