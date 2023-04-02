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

import io.netty.buffer.ByteBuf;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;
import targoss.hardcorealchemy.network.MessageToClient;
import targoss.hardcorealchemy.util.MiscVanilla;

public class MessageHumanity extends MessageToClient<HardcoreAlchemyCreatures> {
    
    public MessageHumanity() {}
    
    public double humanity;
    public boolean syncLastHumanity;
    public double lastHumanity;
    public double magicInhibition;
    public boolean isHumanFormInPhylactery;
    public boolean hasForgottenHumanForm;
    public boolean hasLostHumanity;
    public boolean hasLostMorphAbility;
    
    public MessageHumanity(ICapabilityHumanity humanity, boolean syncLastHumanity) {
        this.humanity = humanity.getHumanity();
        this.syncLastHumanity = syncLastHumanity;
        this.lastHumanity = humanity.getLastHumanity();
        this.magicInhibition = humanity.getMagicInhibition();
        this.isHumanFormInPhylactery = humanity.getIsHumanFormInPhylactery();
        this.hasForgottenHumanForm = humanity.getHasForgottenHumanForm();
        this.hasLostHumanity = humanity.getHasLostHumanity();
        this.hasLostMorphAbility = humanity.getHasForgottenMorphAbility();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(humanity);
        buf.writeBoolean(syncLastHumanity);
        if (syncLastHumanity) {
            buf.writeDouble(lastHumanity);
        }
        buf.writeDouble(magicInhibition);
        buf.writeBoolean(isHumanFormInPhylactery);
        buf.writeBoolean(hasForgottenHumanForm);
        buf.writeBoolean(hasLostHumanity);
        buf.writeBoolean(hasLostMorphAbility);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        humanity = buf.readDouble();
        syncLastHumanity = buf.readBoolean();
        if (syncLastHumanity) {
            lastHumanity = buf.readDouble();
        }
        magicInhibition = buf.readDouble();
        isHumanFormInPhylactery = buf.readBoolean();
        hasForgottenHumanForm = buf.readBoolean();
        hasLostHumanity = buf.readBoolean();
        hasLostMorphAbility = buf.readBoolean();
    }
    
    public static class ReceiveAction implements Runnable {
        @CapabilityInject(ICapabilityHumanity.class)
        public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
        
        private final double humanity;
        private final boolean syncLastHumanity;
        private final double lastHumanity;
        private final double magicInhibition;
        private final boolean isHumanFormInPhylactery;
        private final boolean hasForgottenHumanForm;
        private final boolean hasLostHumanity;
        private final boolean hasLostMorphAbility;
        
        public ReceiveAction(double humanity, boolean syncLastHumanity, double lastHumanity, double magicInhibition,
                boolean isHumanFormInPhylactery, boolean hasForgottenHumanForm, boolean hasLostHumanity, boolean hasLostMorphAbility) {
            this.humanity = humanity;
            this.syncLastHumanity = syncLastHumanity;
            this.lastHumanity = lastHumanity;
            this.magicInhibition = magicInhibition;
            this.isHumanFormInPhylactery = isHumanFormInPhylactery;
            this.hasForgottenHumanForm = hasForgottenHumanForm;
            this.hasLostHumanity = hasLostHumanity;
            this.hasLostMorphAbility = hasLostMorphAbility;
        }
        
        @Override
        public void run() {
            ICapabilityHumanity humanity = MiscVanilla.getTheMinecraftPlayer().getCapability(HUMANITY_CAPABILITY, null);
            if (humanity != null) {
                humanity.setHumanity(this.humanity);
                if (this.syncLastHumanity) {
                    humanity.setLastHumanity(this.lastHumanity);
                }
                humanity.setMagicInhibition(this.magicInhibition);
                humanity.setIsHumanFormInPhylactery(this.isHumanFormInPhylactery);
                humanity.setHasForgottenHumanForm(this.hasForgottenHumanForm);
                humanity.setHasLostHumanity(this.hasLostHumanity);
                humanity.setHasForgottenMorphAbility(this.hasLostMorphAbility);
            }
        }
    }
    
    public static class Handler implements IMessageHandler<MessageHumanity, IMessage> {
        @Override
        public IMessage onMessage(MessageHumanity message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(
                    new ReceiveAction(
                            message.humanity, message.syncLastHumanity, message.lastHumanity, message.magicInhibition,
                            message.isHumanFormInPhylactery, message.hasForgottenHumanForm, message.hasLostHumanity, message.hasLostMorphAbility)
                    );
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient<HardcoreAlchemyCreatures>, IMessage>> getHandlerClass() {
        return Handler.class;
    }
}
