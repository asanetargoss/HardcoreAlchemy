/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures.network;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;
import targoss.hardcorealchemy.creatures.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.creatures.capability.instinct.ProviderInstinct;
import targoss.hardcorealchemy.creatures.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.creatures.instinct.internal.InstinctNeedWrapper;
import targoss.hardcorealchemy.creatures.instinct.network.api.INeedMessenger;
import targoss.hardcorealchemy.network.MessageToClient;
import targoss.hardcorealchemy.util.MiscVanilla;

public class MessageInstinctNeedChanged extends MessageToClient<HardcoreAlchemyCreatures> {
    public MessageInstinctNeedChanged() {}
    
    // The indices at which the need is located
    // Same as in MessageInstinctNeedState, we assume that the instincts are the same on the client and server
    public int instinctID;
    public int needID;
    // Need-specific custom data
    public ByteBuf payload;
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public MessageInstinctNeedChanged(int instinctID, int needID, InstinctNeedWrapper wrapper) {
        this.instinctID = instinctID;
        this.needID = needID;
        IInstinctNeed need = wrapper.need;
        INeedMessenger messenger = wrapper.state.messenger;
        payload = Unpooled.buffer();
        messenger.toBytes(need, this.payload);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(instinctID);
        buf.writeInt(needID);
        buf.writeBytes(payload);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        instinctID = buf.readInt();
        needID = buf.readInt();
        // Copying the data is less efficient, but we should never assume the source buffer won't be re-used later...
        payload = buf.readBytes(buf.readableBytes());
    }
    
    public static class ReceiveAction implements Runnable {
        private int instinctID;
        private int needID;
        private ByteBuf payload;
        
        public ReceiveAction(int instinctID, int needID, ByteBuf payload) {
            this.instinctID = instinctID;
            this.needID = needID;
            this.payload = payload;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
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
            InstinctNeedWrapper wrapper = needs.get(needID);
            IInstinctNeed need = wrapper.getNeed(player);
            INeedMessenger messenger = wrapper.state.messenger;
            messenger.fromBytes(need, payload);
        }
    }
    
    public static class Handler implements IMessageHandler<MessageInstinctNeedChanged, IMessage> {
        @Override
        public IMessage onMessage(MessageInstinctNeedChanged message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(
                    new ReceiveAction(message.instinctID, message.needID, message.payload)
                );
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient<HardcoreAlchemyCreatures>, IMessage>> getHandlerClass() {
        return Handler.class;
    }

}
