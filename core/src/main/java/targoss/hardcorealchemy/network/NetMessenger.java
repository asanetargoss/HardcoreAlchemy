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
    
    public NetMessenger(String channelId) {
        // Throw exception if channelId is too long (due to restrictions in SPacketCustomPayload.readPacketData)
        if (channelId.length() > 20) {
            throw new IllegalArgumentException("Simple channel IDs cannot be longer than 20 characters due to parsing restrictions in vanilla Minecraft code");
        }
        messenger = NetworkRegistry.INSTANCE.newSimpleChannel(channelId);
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
    
    public void sendToAllAround(MessageToClient<MOD> message, NetworkRegistry.TargetPoint point) {
        messenger.sendToAllAround(message, point);
    }
    
    public void sendToServer(RequestToServer<MOD> request) {
        messenger.sendToServer(request);
    }
}
