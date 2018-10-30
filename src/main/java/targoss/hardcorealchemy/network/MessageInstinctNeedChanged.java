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

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.capability.instinct.ProviderInstinct;
import targoss.hardcorealchemy.capability.instinct.StorageInstinct;
import targoss.hardcorealchemy.instinct.api.InstinctNeedWrapper;
import targoss.hardcorealchemy.util.MiscVanilla;

public class MessageInstinctNeedChanged extends MessageToClient {
    public MessageInstinctNeedChanged() {}
    
    // The indices at which the need is located
    // Same as in MessageInstinctNeedState, we assume that the instincts are the same on the client and server
    public int instinctID;
    public int needID;
    public NBTTagCompound needData;
    
    public MessageInstinctNeedChanged(int instinctID, int needID, InstinctNeedWrapper wrapper) {
        this.instinctID = instinctID;
        this.needID = needID;
        this.needData = wrapper.need.serializeNBT();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(instinctID);
        buf.writeInt(needID);
        ByteBufUtils.writeTag(buf, needData);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        instinctID = buf.readInt();
        needID = buf.readInt();
        needData = ByteBufUtils.readTag(buf);
    }
    
    public static class ReceiveAction implements Runnable {
        private int instinctID;
        private int needID;
        private NBTTagCompound needData;
        
        public ReceiveAction(int instinctID, int needID, NBTTagCompound needData) {
            this.instinctID = instinctID;
            this.needID = needID;
            this.needData = needData;
        }

        @Override
        public void run() {
            EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
            ICapabilityInstinct instinct = player.getCapability(ProviderInstinct.INSTINCT_CAPABILITY, null);
            if (instinct == null) {
                return;
            }
            
            List<ICapabilityInstinct.InstinctEntry> instincts = instinct.getInstincts();
            if (instincts.size() == 0) {
                // Not initialized yet because the player has just lost their form this session and the client hasn't figured it out yet
                // TODO: See, stuff like THIS is why MorphState.forceForm should be server-authoritative
                // Don't do some dirty hack where you shove this in another thread to be called later.
                // Leave this small-likelihood packet loss bug in here, then properly fix the issue in MorphState.forceForm
                return;
            }
            List<InstinctNeedWrapper> needs = instincts.get(instinctID).getNeeds(player);
            needs.get(needID).need.deserializeNBT(needData);
        }
    }
    
    public static class Handler implements IMessageHandler<MessageInstinctNeedChanged, IMessage> {
        @Override
        public IMessage onMessage(MessageInstinctNeedChanged message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(
                    new ReceiveAction(message.instinctID, message.needID, message.needData)
                );
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient, IMessage>> getHandlerClass() {
        return Handler.class;
    }

}
