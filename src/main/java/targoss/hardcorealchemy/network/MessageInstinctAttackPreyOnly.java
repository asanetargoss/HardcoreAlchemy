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
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.capability.instincts.ICapabilityInstinct;
import targoss.hardcorealchemy.capability.instincts.ICapabilityInstinct.InstinctEntry;
import targoss.hardcorealchemy.capability.instincts.ProviderInstinct;
import targoss.hardcorealchemy.instinct.InstinctAttackPreyOnly;
import targoss.hardcorealchemy.instinct.Instincts;
import targoss.hardcorealchemy.util.MiscVanilla;

public class MessageInstinctAttackPreyOnly extends MessageToClient implements Runnable {
    public boolean active;
    public int numKills;
    public int requiredKills;
    public boolean inKillingFrenzy;
    
    public MessageInstinctAttackPreyOnly() {}
    
    public MessageInstinctAttackPreyOnly(InstinctAttackPreyOnly attackPrey) {
        active = attackPrey.active;
        numKills = attackPrey.numKills;
        requiredKills = attackPrey.requiredKills;
        inKillingFrenzy = attackPrey.inKillingFrenzy;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(active);
        buf.writeInt(numKills);
        buf.writeInt(requiredKills);
        buf.writeBoolean(inKillingFrenzy);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        active = buf.readBoolean();
        numKills = buf.readInt();
        requiredKills = buf.readInt();
        inKillingFrenzy = buf.readBoolean();
    }

    @Override
    public void run() {
        EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
        ICapabilityInstinct instinct = player.getCapability(ProviderInstinct.INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        InstinctEntry entry = instinct.getInstinctMap().get(Instincts.ATTACK_PREY_ONLY.getRegistryName());
        if (entry == null) {
            return;
        }
        InstinctAttackPreyOnly attackPrey = (InstinctAttackPreyOnly)entry.instinct;
        
        attackPrey.active = active;
        attackPrey.numKills = numKills;
        attackPrey.requiredKills = requiredKills;
        attackPrey.inKillingFrenzy = inKillingFrenzy;
    }
    
    public static class Handler implements IMessageHandler<MessageInstinctAttackPreyOnly, IMessage> {
        @Override
        public IMessage onMessage(MessageInstinctAttackPreyOnly message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(message);
            return null;
        }
        
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient, IMessage>> getHandlerClass() {
        return Handler.class;
    }
}
