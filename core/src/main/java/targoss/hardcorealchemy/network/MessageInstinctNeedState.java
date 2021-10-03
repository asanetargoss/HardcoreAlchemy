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
import targoss.hardcorealchemy.instinct.internal.InstinctNeedWrapper;
import targoss.hardcorealchemy.util.MiscVanilla;

/**
 * Tells the client that instinct value/need states have been changed
 */
public class MessageInstinctNeedState extends MessageToClient {

    public MessageInstinctNeedState() { }
    
    public static class NeedStateData {
        public float level;
        // (byte)NeedStatus.ordinal()
        public byte state;
    }
    
    public List<List<NeedStateData>> needStatesPerInstinct = new ArrayList<>();
    
    public MessageInstinctNeedState(ICapabilityInstinct instinct) {
        for (ICapabilityInstinct.InstinctEntry entry : instinct.getInstincts()) {
            List<NeedStateData> states = new ArrayList<>();
            for (InstinctNeedWrapper wrapper : entry.needs) {
                NeedStateData state = new NeedStateData();
                state.level = wrapper.state.instinct;
                state.state = (byte)wrapper.state.needStatus.ordinal();
                states.add(state);
            }
            needStatesPerInstinct.add(states);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        int numInstincts = needStatesPerInstinct.size();
        buf.writeInt(numInstincts);
        for (List<NeedStateData> states : needStatesPerInstinct) {
            buf.writeInt(states.size());
            for (NeedStateData state : states) {
                buf.writeFloat(state.level);
                buf.writeByte(state.state);
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int numInstincts = buf.readInt();
        for (int i = 0; i < numInstincts; i++) {
            List<NeedStateData> states = new ArrayList<>();
            int numStates = buf.readInt();
            for (int j = 0; j < numStates; j++) {
                NeedStateData state = new NeedStateData();
                state.level = buf.readFloat();
                state.state = buf.readByte();
                states.add(state);
            }
            needStatesPerInstinct.add(states);
        }
    }
    
    protected static boolean doSizesMatch(EntityPlayer player, List<ICapabilityInstinct.InstinctEntry> instincts, List<List<NeedStateData>> needStatesPerInstinct) {
        if (instincts.size() != needStatesPerInstinct.size()) {
            return false;
        }
        int n = instincts.size();
        for (int i = 0; i < n; i++) {
            if (instincts.get(i).getNeeds(player).size() != needStatesPerInstinct.get(i).size()) {
                return false;
            }
        }
        return true;
    }
    
    public static class ReceiveAction implements Runnable {
        private List<List<NeedStateData>> needStatesPerInstinct = new ArrayList<>();
        
        public ReceiveAction(List<List<NeedStateData>> needStatesPerInstinct) {
            this.needStatesPerInstinct = needStatesPerInstinct;
        }

        @Override
        public void run() {
            EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
            ICapabilityInstinct instinct = player.getCapability(ProviderInstinct.INSTINCT_CAPABILITY, null);
            if (instinct == null) {
                return;
            }
            
            // Sync which needs are/are not being met
            List<ICapabilityInstinct.InstinctEntry> instincts = instinct.getInstincts();
            if (!doSizesMatch(player, instincts, needStatesPerInstinct)) {
                HardcoreAlchemy.LOGGER.error("Could not sync instinct need statuses because the instincts are out of sync");
            }
            else {
                int n = instincts.size();
                for (int i = 0; i < n; i++) {
                    List<InstinctNeedWrapper> needs = instincts.get(i).getNeeds(player);
                    List<NeedStateData> newStates = needStatesPerInstinct.get(i);
                    int m = needs.size();
                    for (int j = 0; j < m; j++) {
                        InstinctNeedWrapper need = needs.get(j);
                        NeedStateData newState = newStates.get(j);
                        need.state.instinct = newState.level;
                        need.state.needStatus = IInstinctState.NeedStatus.values()[newState.state];
                    }
                }
            }
        }
    }
    
    public static class Handler implements IMessageHandler<MessageInstinctNeedState, IMessage> {
        @Override
        public IMessage onMessage(MessageInstinctNeedState message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(new ReceiveAction(message.needStatesPerInstinct));
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient, IMessage>> getHandlerClass() {
        return Handler.class;
    }
}
