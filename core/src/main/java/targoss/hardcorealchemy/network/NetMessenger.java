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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetMessenger<MOD> {
    protected final SimpleNetworkWrapper messenger;
    private int nextId = 0;
    
    public NetMessenger(String modId) {
        messenger = NetworkRegistry.INSTANCE.newSimpleChannel(modId);
    }
    
    @SuppressWarnings("unchecked")
    public NetMessenger<MOD> register(MessageToClient<MOD> message) {
        messenger.registerMessage(
                (Class<IMessageHandler<MessageToClient<MOD>, IMessage>>)message.getHandlerClass(),
                (Class<MessageToClient<MOD>>)message.getClass(),
                nextId++,
                Side.CLIENT
            );
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public NetMessenger<MOD> register(RequestToServer<MOD> request) {
        messenger.registerMessage(
                (Class<IMessageHandler<RequestToServer<MOD>, IMessage>>)request.getHandlerClass(),
                (Class<RequestToServer<MOD>>)request.getClass(),
                nextId++,
                Side.SERVER
            );
        return this;
    }
    
    public void sendTo(MessageToClient<MOD> message, EntityPlayerMP player) {
        messenger.sendTo(message, player);
    }
    
    public void sendToServer(RequestToServer<MOD> request) {
        messenger.sendToServer(request);
    }
}