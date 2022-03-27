/*
 * Copyright 2019 asanetargoss
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
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.config.Configs;

public class MessageConfigs extends MessageToClient<HardcoreAlchemy> {
    
    public MessageConfigs() {
        this.configs = new Configs();
    }
    
    public MessageConfigs(Configs configs) {
        this.configs = configs;
    }
    
    public Configs configs;

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(configs.base.version);
        buf.writeBoolean(configs.base.enableHearts);
        buf.writeBoolean(configs.base.enableInstincts);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        configs.base.version = buf.readInt();
        configs.base.enableHearts = buf.readBoolean();
        configs.base.enableInstincts = buf.readBoolean();
    }
    
    public static class ReceiveAction implements Runnable {
        private final Configs configs = new Configs();
        
        public ReceiveAction(Configs configs) {
            this.configs.base.version = configs.base.version;
            this.configs.base.enableHearts = configs.base.enableHearts;
            this.configs.base.enableInstincts = configs.base.enableInstincts;
        }

        @Override
        public void run() {
            Configs clientConfigs = HardcoreAlchemy.proxy.configs;
            // TODO: More robust config networking. This is okay for now.
            if (clientConfigs.base.version != configs.base.version) {
                HardcoreAlchemy.LOGGER.warn("Server config version is " + configs.base.version +
                        ", but client config version is " + clientConfigs.base.version +
                        ". Reading the config packet has been aborted. This could cause serious desyncs!");
                return;
            }
            clientConfigs.base.enableInstincts = configs.base.enableInstincts;
        }
    }
    
    public static class Handler implements IMessageHandler<MessageConfigs, IMessage> {
        @Override
        public IMessage onMessage(MessageConfigs message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(new ReceiveAction(message.configs));
            return null;
        }
        
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient<HardcoreAlchemy>, IMessage>> getHandlerClass() {
        return Handler.class;
    }

}
