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

package targoss.hardcorealchemy.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import targoss.hardcorealchemy.coremod.CoremodHook;

public abstract class EventTakeStack extends Event {
    public final Slot slot;
    public final EntityPlayer player;
    
    public EventTakeStack(Slot slot, EntityPlayer player) {
        this.slot = slot;
        this.player = player;
    }
    
    @Cancelable
    public static class Pre extends EventTakeStack {
        private boolean canceled = false;
        
        public Pre(Slot slot, EntityPlayer player) {
            super(slot, player);
        }
        
        public boolean isCanceled() {
            return canceled;
        }
        
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
        
        @CoremodHook
        public static boolean onTakeStackPre(Slot slot, EntityPlayer player) {
            Pre event = new Pre(slot, player);
            return !(MinecraftForge.EVENT_BUS.post(event));
        }
    }
}
