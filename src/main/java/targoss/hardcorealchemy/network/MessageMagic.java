/**
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

package targoss.hardcorealchemy.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.listener.ListenerPlayerMagic;

public class MessageMagic extends MessageToClient {
    
    public MessageMagic() {}
    
    public boolean canUseHighMagic;
    
    public MessageMagic(boolean canUseHighMagic) {
        this.canUseHighMagic = canUseHighMagic;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
        this.canUseHighMagic = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(canUseHighMagic);
    }
    
    public static class ReceiveAction implements Runnable {
        private boolean canUseHighMagic;
        
        public ReceiveAction(boolean canUseHighMagic) {
            this.canUseHighMagic = canUseHighMagic;
        }
        
        @Override
        public void run() {
            ListenerPlayerMagic.canUseHighMagic = this.canUseHighMagic;
        }
    }
    
    public static class Handler implements IMessageHandler<MessageMagic, IMessage> {

        @Override
        public IMessage onMessage(MessageMagic message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(new ReceiveAction(message.canUseHighMagic));
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient, IMessage>> getHandlerClass() {
        return Handler.class;
    }

}
