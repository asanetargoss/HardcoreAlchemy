/*
 * Copyright 2017-2023 asanetargoss
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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import targoss.hardcorealchemy.coremod.CoremodHook;

public class EventHeldItemChange extends Event {
    public final CPacketHeldItemChange packet;
    public final EntityPlayerMP player;
    
    protected EventHeldItemChange(CPacketHeldItemChange packet, EntityPlayerMP player) {
        this.packet = packet;
        this.player = player;
    }
    
    public static class Pre extends EventHeldItemChange {
        public Pre(CPacketHeldItemChange packet, EntityPlayerMP player) {
            super(packet, player);
        }
    }
    
    public static class Post extends EventHeldItemChange {
        public Post(CPacketHeldItemChange packet, EntityPlayerMP player) {
            super(packet, player);
        }
    }
    
    @CoremodHook
    public static void onHeldItemChangePre(CPacketHeldItemChange packet, EntityPlayerMP player) {
        EventHeldItemChange.Pre event = new EventHeldItemChange.Pre(packet, player);
        MinecraftForge.EVENT_BUS.post(event);
    }

    @CoremodHook
    public static void onHeldItemChangePost(CPacketHeldItemChange packet, EntityPlayerMP player) {
        EventHeldItemChange.Post event = new EventHeldItemChange.Post(packet, player);
        MinecraftForge.EVENT_BUS.post(event);
    }
}
