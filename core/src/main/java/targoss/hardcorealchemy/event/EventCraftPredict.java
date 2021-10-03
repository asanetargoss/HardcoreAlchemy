/*
 * Copyright 2017-2018 asanetargoss
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

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import targoss.hardcorealchemy.coremod.CoremodHook;

@Cancelable
public class EventCraftPredict extends Event {
    private boolean canceled = false;
    
    public ItemStack craftResult;
    public final InventoryCrafting craftGrid;
    public final World world;
    
    public EventCraftPredict(ItemStack itemStack, InventoryCrafting inventoryCrafting, World world) {
        this.craftResult = itemStack;
        this.craftGrid = inventoryCrafting;
        this.world = world;
    }
    
    public boolean isCanceled() {
        return canceled;
    }
    
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
    
    @CoremodHook
    public static ItemStack onCraftPredict(ItemStack craftResult, InventoryCrafting craftGrid, World world) {
        EventCraftPredict event = new EventCraftPredict(craftResult, craftGrid, world);
        return (MinecraftForge.EVENT_BUS.post(event) ? null : event.craftResult);
    }
}
