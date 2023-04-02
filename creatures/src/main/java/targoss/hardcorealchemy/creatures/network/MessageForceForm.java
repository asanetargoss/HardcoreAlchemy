/*
 * Copyright 2017-2023 asanetargoss
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

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import mchorse.mclib.utils.NBTUtils;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.MorphUtils;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.capability.humanity.MorphAbilityChangeReason;
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerHumanity;
import targoss.hardcorealchemy.creatures.util.MorphState;
import targoss.hardcorealchemy.network.MessageToClient;
import targoss.hardcorealchemy.util.MiscVanilla;

public class MessageForceForm extends MessageToClient<HardcoreAlchemyCreatures> {
    public MorphAbilityChangeReason reason;
    public AbstractMorph lastMorph;
    public AbstractMorph morph;
    
    public MessageForceForm() {}
    
    public MessageForceForm(MorphAbilityChangeReason reason, @Nullable AbstractMorph lastMorph, @Nullable AbstractMorph morph) {
        this.reason = reason;
        this.lastMorph = lastMorph;
        this.morph = morph;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(reason.ordinal());
        MorphUtils.morphToBuf(buf, lastMorph);
        MorphUtils.morphToBuf(buf, morph);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        byte reasonNum = buf.readByte();
        if (reasonNum > MorphAbilityChangeReason.values().length) {
            reasonNum = 0;
        }
        reason = MorphAbilityChangeReason.values()[reasonNum];
        lastMorph = MorphManager.INSTANCE.morphFromNBT(NBTUtils.readInfiniteTag(buf));
        morph = MorphManager.INSTANCE.morphFromNBT(NBTUtils.readInfiniteTag(buf));
    }

    public static class ReceiveAction implements Runnable {
        private MorphAbilityChangeReason reason;
        private AbstractMorph lastMorph;
        private AbstractMorph morph;
        
        public ReceiveAction(MorphAbilityChangeReason reason, @Nullable AbstractMorph lastMorph, @Nullable AbstractMorph morph) {
            this.reason = reason;
            this.lastMorph = lastMorph;
            this.morph = morph;
        }
        
        @Override
        public void run() {
            EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
            IMorphing morphing = player.getCapability(ListenerPlayerHumanity.MORPHING_CAPABILITY, null);
            if (morphing != null) {
                MorphState.forceForm(HardcoreAlchemyCore.proxy.configs, player, reason, morphing, lastMorph, morph);
            }
        }
    }
    
    public static class Handler implements IMessageHandler<MessageForceForm, IMessage> {
        @Override
        public IMessage onMessage(MessageForceForm message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(
                    new ReceiveAction(message.reason, message.lastMorph, message.morph)
                    );
            return null;
        }
        
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient<HardcoreAlchemyCreatures>, IMessage>> getHandlerClass() {
        return Handler.class;
    }

}
