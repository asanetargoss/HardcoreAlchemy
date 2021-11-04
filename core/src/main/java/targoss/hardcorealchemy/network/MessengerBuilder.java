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

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class MessengerBuilder {
    protected final SimpleNetworkWrapper messenger;
    private static int nextId = 0;
    
    public MessengerBuilder(String modId) {
        messenger = NetworkRegistry.INSTANCE.newSimpleChannel(modId);
    }
    
    @SuppressWarnings("unchecked")
    public MessengerBuilder register(MessageToClient message) {
        messenger.registerMessage(
                (Class<IMessageHandler<MessageToClient, IMessage>>)message.getHandlerClass(),
                (Class<MessageToClient>)message.getClass(),
                nextId++,
                Side.CLIENT
            );
        return this;
    }
    
    @SuppressWarnings("unchecked")
    public MessengerBuilder register(RequestToServer request) {
        messenger.registerMessage(
                (Class<IMessageHandler<RequestToServer, IMessage>>)request.getHandlerClass(),
                (Class<RequestToServer>)request.getClass(),
                nextId++,
                Side.SERVER
            );
        return this;
    }
    
    public SimpleNetworkWrapper done() {
        return messenger;
    }
}
