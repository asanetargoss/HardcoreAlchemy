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

package targoss.hardcorealchemy.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.listener.ListenerGuiHud;

public class MessageHumanity extends MessageToClient {
    
    public MessageHumanity() {}
    
    public boolean render_humanity;
    public double humanity;
    public double max_humanity;
    
    public MessageHumanity(boolean render_humanity, double humanity, double max_humanity) {
        this.render_humanity = render_humanity;
        this.humanity = humanity;
        this.max_humanity = max_humanity;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(render_humanity);
        buf.writeDouble(humanity);
        buf.writeDouble(max_humanity);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        render_humanity = buf.readBoolean();
        humanity = buf.readDouble();
        max_humanity = buf.readDouble();
    }
    
    public static class ReceiveAction implements Runnable {
        private boolean render_humanity;
        private double humanity;
        private double max_humanity;
        
        public ReceiveAction(boolean render_humanity, double humanity, double max_humanity) {
            this.render_humanity = render_humanity;
            this.humanity = humanity;
            this.max_humanity = max_humanity;
        }
        
        @Override
        public void run() {
            ListenerGuiHud.render_humanity = render_humanity;
            ListenerGuiHud.humanity = humanity;
            ListenerGuiHud.max_humanity = max_humanity;
        }
    }
    
    public static class Handler implements IMessageHandler<MessageHumanity, IMessage> {
        @Override
        public IMessage onMessage(MessageHumanity message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(new ReceiveAction(message.render_humanity, message.humanity, message.max_humanity));
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient, IMessage>> getHandlerClass() {
        return Handler.class;
    }
}
