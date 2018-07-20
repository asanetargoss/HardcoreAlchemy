/*
 * Copyright 2018 asanetargoss
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.capability.instincts.ICapabilityInstinct;
import targoss.hardcorealchemy.capability.instincts.ProviderInstinct;
import targoss.hardcorealchemy.util.MiscVanilla;

/**
 * Tells the client that an instinct has been activated for the player.
 */
public class MessageInstinctActive extends MessageToClient implements Runnable {

    public MessageInstinctActive() { }
    
    public float instinct;
    public ResourceLocation activeInstinct;
    
    public MessageInstinctActive(ICapabilityInstinct instinct) {
        this.instinct = instinct.getInstinct();
        this.activeInstinct = instinct.getActiveInstinctId();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(instinct);
        
        if (activeInstinct != null) {
            buf.writeBoolean(true);
            ByteBufUtils.writeUTF8String(buf, activeInstinct.toString());
        }
        else {
            buf.writeBoolean(false);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.instinct = buf.readFloat();
        
        if (buf.readBoolean() == true) {
            this.activeInstinct = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
        }
        else {
            this.activeInstinct = null;
        }
    }
    
    @Override
    public void run() {
        EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
        ICapabilityInstinct instinct = player.getCapability(ProviderInstinct.INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        
        instinct.setInstinct(this.instinct);
        
        if (instinct.getActiveInstinct() != null) {
            instinct.getActiveInstinct().onDeactivate(player);
        }
        
        instinct.setActiveInstinctId(this.activeInstinct);
        if (this.activeInstinct != null) {
            instinct.getActiveInstinct().onActivate(player);
        }
    }
    
    public static class Handler implements IMessageHandler<MessageInstinctActive, IMessage> {
        @Override
        public IMessage onMessage(MessageInstinctActive message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(message);
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient, IMessage>> getHandlerClass() {
        return Handler.class;
    }
}
