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

package targoss.hardcorealchemy.creatures.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;
import targoss.hardcorealchemy.creatures.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.creatures.capability.instinct.ProviderInstinct;
import targoss.hardcorealchemy.network.MessageToClient;
import targoss.hardcorealchemy.util.MiscVanilla;

public class MessageInstinct extends MessageToClient<HardcoreAlchemyCreatures> {
    
    public MessageInstinct() { }
    
    public NBTTagCompound instinctNBT = new NBTTagCompound();
    
    public MessageInstinct(ICapabilityInstinct instinct) {
        instinctNBT = (NBTTagCompound)ProviderInstinct.INSTINCT_CAPABILITY.writeNBT(instinct, null);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeTag(buf, instinctNBT);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        instinctNBT = ByteBufUtils.readTag(buf);
    }
    
    public static class ReceiveAction implements Runnable {
        private NBTTagCompound instinctNBT;
        
        public ReceiveAction(NBTTagCompound instinctNBT) {
            this.instinctNBT = instinctNBT;
        }
        
        @Override
        public void run() {
            ICapabilityInstinct instinct = MiscVanilla
                    .getTheMinecraftPlayer()
                    .getCapability(ProviderInstinct.INSTINCT_CAPABILITY, null);
            if (instinct == null) {
                return;
            }
            
            ProviderInstinct.INSTINCT_CAPABILITY.readNBT(instinct, null, instinctNBT);
        }
    }
    
    public static class Handler implements IMessageHandler<MessageInstinct, IMessage> {
        @Override
        public IMessage onMessage(MessageInstinct message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(new ReceiveAction(message.instinctNBT));
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient<HardcoreAlchemyCreatures>, IMessage>> getHandlerClass() {
        return Handler.class;
    }
}
