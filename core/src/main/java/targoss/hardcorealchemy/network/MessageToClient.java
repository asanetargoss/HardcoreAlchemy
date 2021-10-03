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

import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public abstract class MessageToClient implements IMessage {
    public static final SimpleNetworkWrapper INSTANCE = PacketHandler.INSTANCE;
    
    public void register() {
        INSTANCE.registerMessage(
                    (Class<IMessageHandler<MessageToClient, IMessage>>)this.getHandlerClass(),
                    (Class<MessageToClient>)this.getClass(),
                    PacketHandler.getNextId(),
                    Side.CLIENT
                );
    }
    
    public static IThreadListener getThreadListener() {
        return Minecraft.getMinecraft();
    }
    
    public abstract Class<? extends IMessageHandler<? extends MessageToClient, IMessage>> getHandlerClass();
}
