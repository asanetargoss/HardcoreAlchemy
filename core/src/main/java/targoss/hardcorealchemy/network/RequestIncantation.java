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

package targoss.hardcorealchemy.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.incantation.IncantationParts;
import targoss.hardcorealchemy.listener.ListenerPlayerIncantation;

public class RequestIncantation extends RequestToServer<HardcoreAlchemy> {
    public IncantationParts parts;

    public RequestIncantation() {}
    
    public RequestIncantation(IncantationParts parts) {
        this.parts = parts;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        parts.toBytes(buf);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        parts = new IncantationParts();
        parts.fromBytes(buf);
    }
    
    public static class ReceiveAction implements Runnable {
        private EntityPlayerMP player;
        private IncantationParts parts;

        public ReceiveAction(EntityPlayerMP player, IncantationParts parts) {
            this.player = player;
            this.parts = parts;
        }

        @Override
        public void run() {
            ListenerPlayerIncantation.invokeSpells(player, parts);
        }
    }
    
    public static class Handler implements IMessageHandler<RequestIncantation, IMessage> {
        @Override
        public IMessage onMessage(RequestIncantation message, MessageContext ctx) {
            if (!message.parts.isEmpty() && message.parts.isValid()) {
                message.getThreadListener(ctx).addScheduledTask(new ReceiveAction(ctx.getServerHandler().playerEntity, message.parts));
            }
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends RequestToServer<HardcoreAlchemy>, IMessage>> getHandlerClass() {
        return Handler.class;
    }

}
