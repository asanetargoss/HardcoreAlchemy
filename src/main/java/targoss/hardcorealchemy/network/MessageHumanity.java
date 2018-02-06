/*
 * Copyright 2017-2018 asanetargoss
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
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.listener.ListenerGuiHud;

public class MessageHumanity extends MessageToClient {
    
    public MessageHumanity() {}
    
    public double humanity;
    public double lastHumanity;
    public boolean hasLostHumanity;
    public boolean hasLostMorphAbility;
    public boolean isMarried;
    public boolean isMage;
    public boolean highMagicOverride;
    
    public MessageHumanity(ICapabilityHumanity humanity) {
        this.humanity = humanity.getHumanity();
        this.lastHumanity = humanity.getLastHumanity();
        this.hasLostHumanity = humanity.getHasLostHumanity();
        this.hasLostMorphAbility = humanity.getHasLostMorphAbility();
        this.isMarried = humanity.getIsMarried();
        this.isMage = humanity.getIsMage();
        this.highMagicOverride = humanity.getHighMagicOverride();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(humanity);
        buf.writeDouble(lastHumanity);
        buf.writeBoolean(hasLostHumanity);
        buf.writeBoolean(hasLostMorphAbility);
        buf.writeBoolean(isMarried);
        buf.writeBoolean(isMage);
        buf.writeBoolean(highMagicOverride);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        humanity = buf.readDouble();
        lastHumanity = buf.readDouble();
        hasLostHumanity = buf.readBoolean();
        hasLostMorphAbility = buf.readBoolean();
        isMarried = buf.readBoolean();
        isMage = buf.readBoolean();
        highMagicOverride = buf.readBoolean();
    }
    
    public static class ReceiveAction implements Runnable {
        @CapabilityInject(ICapabilityHumanity.class)
        public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
        
        private double humanity;
        private double lastHumanity;
        private boolean hasLostHumanity;
        private boolean hasLostMorphAbility;
        private boolean isMarried;
        private boolean isMage;
        private boolean highMagicOverride;
        
        public ReceiveAction(double humanity, double lastHumanity, boolean hasLostHumanity,
                boolean hasLostMorphAbility, boolean isMarried, boolean isMage,
                boolean highMagicOverride) {
            this.humanity = humanity;
            this.lastHumanity = lastHumanity;
            this.hasLostHumanity = hasLostHumanity;
            this.hasLostMorphAbility = hasLostMorphAbility;
            this.isMarried = isMarried;
            this.highMagicOverride = highMagicOverride;
        }
        
        @Override
        public void run() {
            ICapabilityHumanity humanity = Minecraft.getMinecraft().thePlayer.getCapability(HUMANITY_CAPABILITY, null);
            if (humanity != null) {
                humanity.setHumanity(this.humanity);
                humanity.setLastHumanity(this.lastHumanity);
                humanity.setHasLostHumanity(this.hasLostHumanity);
                humanity.setHasLostMorphAbility(this.hasLostMorphAbility);
                humanity.setIsMarried(this.isMarried);
                humanity.setIsMage(this.isMage);
                humanity.setHighMagicOverride(this.highMagicOverride);
            }
        }
    }
    
    public static class Handler implements IMessageHandler<MessageHumanity, IMessage> {
        @Override
        public IMessage onMessage(MessageHumanity message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(
                    new ReceiveAction(
                            message.humanity, message.lastHumanity, message.hasLostHumanity,
                            message.hasLostMorphAbility, message.isMarried, message.isMage,
                            message.highMagicOverride
                            )
                    );
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient, IMessage>> getHandlerClass() {
        return Handler.class;
    }
}
