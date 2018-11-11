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

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.util.MiscVanilla;

@Cancelable
public class EventDrawHeldItem extends Event {
    public ItemStack itemStack;
    
    public EventDrawHeldItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
    
    @CoremodHook
    public static ItemStack onDrawHeldItem(ItemStack itemStack) {
        ItemStack newItemStack = MiscVanilla.isEmptyItemStack(itemStack) ? itemStack : itemStack.copy();
        EventDrawHeldItem event = new EventDrawHeldItem(newItemStack);
        return (MinecraftForge.EVENT_BUS.post(event) ? MiscVanilla.ITEM_STACK_EMPTY : event.itemStack);
    }
}
