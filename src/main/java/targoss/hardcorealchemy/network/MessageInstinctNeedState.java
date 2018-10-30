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

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.capability.instinct.ProviderInstinct;
import targoss.hardcorealchemy.instinct.api.IInstinctState;
import targoss.hardcorealchemy.instinct.api.InstinctNeedWrapper;
import targoss.hardcorealchemy.util.MiscVanilla;

/**
 * Tells the client that instinct value/need states have been changed
 */
public class MessageInstinctNeedState extends MessageToClient implements Runnable {

    public MessageInstinctNeedState() { }
    
    public float instinct;
    // (byte)NeedStatus.ordinal()
    public List<List<Byte>> statesPerInstinct = new ArrayList<>();
    
    public MessageInstinctNeedState(ICapabilityInstinct instinct) {
        this.instinct = instinct.getInstinct();
        for (ICapabilityInstinct.InstinctEntry entry : instinct.getInstincts()) {
            List<Byte> instinctStates = new ArrayList<>();
            for (InstinctNeedWrapper wrapper : entry.needs) {
                instinctStates.add((byte)wrapper.state.needStatus.ordinal());
            }
            statesPerInstinct.add(instinctStates);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(instinct);
        buf.writeInt(statesPerInstinct.size());
        for (List<Byte> statuses : statesPerInstinct) {
            buf.writeInt(statuses.size());
            for (Byte status : statuses) {
                buf.writeByte(status);
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.instinct = buf.readFloat();
        int numInstincts = buf.readInt();
        for (int i = 0; i < numInstincts; i++) {
            List<Byte> statuses = new ArrayList<>();
            int numStatuses = buf.readInt();
            for (int j = 0; j < numStatuses; j++) {
                statuses.add(buf.readByte());
            }
            statesPerInstinct.add(statuses);
        }
    }
    
    private boolean doSizesMatch(List<ICapabilityInstinct.InstinctEntry> instincts, List<List<Byte>> statesPerInstinct) {
        if (instincts.size() != statesPerInstinct.size()) {
            return false;
        }
        int n = instincts.size();
        for (int i = 0; i < n; i++) {
            if (instincts.get(i).needs.size() != statesPerInstinct.get(i).size()) {
                return false;
            }
        }
        return true;
    }
    
    // TODO: Move to static class because this ends up getting called on the wrong thread
    @Override
    public void run() {
        EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
        ICapabilityInstinct instinct = player.getCapability(ProviderInstinct.INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        
        instinct.setInstinct(this.instinct);
        
        // Sync which needs are/are not being met
        List<ICapabilityInstinct.InstinctEntry> instincts = instinct.getInstincts();
        if (!doSizesMatch(instincts, statesPerInstinct)) {
            HardcoreAlchemy.LOGGER.error("Could not sync instinct need statuses because the instincts are out of sync");
        }
        else {
            int n = instincts.size();
            for (int i = 0; i < n; i++) {
                List<InstinctNeedWrapper> needs = instincts.get(i).needs;
                List<Byte> states = statesPerInstinct.get(i);
                int m = needs.size();
                for (int j = 0; j < m; j++) {
                    needs.get(j).state.needStatus = IInstinctState.NeedStatus.values()[states.get(j)];
                }
            }
        }
    }
    
    public static class Handler implements IMessageHandler<MessageInstinctNeedState, IMessage> {
        @Override
        public IMessage onMessage(MessageInstinctNeedState message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(message);
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient, IMessage>> getHandlerClass() {
        return Handler.class;
    }
}
