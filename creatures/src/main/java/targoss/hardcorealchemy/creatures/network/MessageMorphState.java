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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;
import targoss.hardcorealchemy.creatures.capability.morphstate.ICapabilityMorphState;
import targoss.hardcorealchemy.network.MessageToClient;
import targoss.hardcorealchemy.util.MiscVanilla;

public class MessageMorphState extends MessageToClient<HardcoreAlchemyCreatures> {
    public MessageMorphState() { }
    
    public boolean isFishingUnderwater = false;
    public int fishingTimer = 0;
    
    public MessageMorphState(ICapabilityMorphState morphState) {
        this.isFishingUnderwater = morphState.getIsFishingUnderwater();
        this.fishingTimer = morphState.getFishingTimer();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        isFishingUnderwater = buf.readBoolean();
        fishingTimer = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(isFishingUnderwater);
        buf.writeInt(fishingTimer);
    }
    
    public static class ReceiveAction implements Runnable {
        @CapabilityInject(ICapabilityMorphState.class)
        private static final Capability<ICapabilityMorphState> MORPH_STATE_CAPABILITY = null;
        
        private boolean isFishingUnderwater;
        private int fishingTimer;
        
        public ReceiveAction(boolean isFishingUnderwater, int fishingTimer) {
            this.isFishingUnderwater = isFishingUnderwater;
            this.fishingTimer = fishingTimer;
        }
        
        @Override
        public void run() {
            ICapabilityMorphState morphState = MiscVanilla.getTheMinecraftPlayer().getCapability(MORPH_STATE_CAPABILITY, null);
            if (morphState != null) {
                morphState.setIsFishingUnderwater(this.isFishingUnderwater);
                morphState.setFishingTimer(this.fishingTimer);
            }
        }
    }
    
    public static class Handler implements IMessageHandler<MessageMorphState, IMessage> {
        @Override
        public IMessage onMessage(MessageMorphState message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(
                    new ReceiveAction(message.isFishingUnderwater, message.fishingTimer)
                    );
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient<HardcoreAlchemyCreatures>, IMessage>> getHandlerClass() {
        return Handler.class;
    }

}
