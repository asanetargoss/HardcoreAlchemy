/*
 * Copyright 2020 asanetargoss
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

import javax.annotation.Nullable;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.coremod.CoremodHook;

@SideOnly(Side.CLIENT)
public class EventSendChatMessage extends Event {
    public final EntityPlayerSP player;
    public String message;
    
    public EventSendChatMessage(EntityPlayerSP player, String message) {
        this.player = player;
        this.message = message;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }
    
    @CoremodHook
    public static @Nullable String onSendChatMessage(EntityPlayerSP player, String message) {
        EventSendChatMessage event = new EventSendChatMessage(player, message);
        boolean canceled = MinecraftForge.EVENT_BUS.post(event);
        return canceled ? null : event.message;
    }
}
