/*
 * Copyright 2017-2022 asanetargoss
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

package targoss.hardcorealchemy.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.config.Configs;

public class MessageConfigs extends MessageToClient<HardcoreAlchemyCore> {
    
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
            Configs clientConfigs = HardcoreAlchemyCore.proxy.configs;
            // TODO: More robust config networking. This is okay for now.
            if (clientConfigs.base.version != configs.base.version) {
                HardcoreAlchemyCore.LOGGER.warn("Server config version is " + configs.base.version +
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
    public Class<? extends IMessageHandler<? extends MessageToClient<HardcoreAlchemyCore>, IMessage>> getHandlerClass() {
        return Handler.class;
    }

}
