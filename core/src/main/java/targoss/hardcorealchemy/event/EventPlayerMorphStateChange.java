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
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired by MorphState.forceForm on both the client and server side.
 * It is recommended to not send any packets and instead wait for
 * EventPlayerMorphStateChange to be fired on the client.
 */
public class EventPlayerMorphStateChange extends Event {
    public final EntityPlayer player;
    
    protected EventPlayerMorphStateChange(EntityPlayer player) {
        this.player = player;
    }
    
    public static class Post extends EventPlayerMorphStateChange {
        public Post(EntityPlayer player) {
            super(player);
        }
    }
}
