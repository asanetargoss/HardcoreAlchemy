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

import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public abstract class RequestToServer implements IMessage {
    public static final SimpleNetworkWrapper INSTANCE = PacketHandler.INSTANCE;
    
    @SuppressWarnings("unchecked")
    public void register() {
        INSTANCE.registerMessage(
                    (Class<IMessageHandler<RequestToServer, IMessage>>)this.getHandlerClass(),
                    (Class<RequestToServer>)this.getClass(),
                    PacketHandler.getNextId(),
                    Side.SERVER
                );
    }
    
    public static IThreadListener getThreadListener(MessageContext ctx) {
        return FMLCommonHandler.instance().getWorldThread(ctx.getServerHandler());
    }
    
    public abstract Class<? extends IMessageHandler<? extends RequestToServer, IMessage>> getHandlerClass();
}
