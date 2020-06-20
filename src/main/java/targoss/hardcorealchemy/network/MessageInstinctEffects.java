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

import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.capability.instinct.ProviderInstinct;
import targoss.hardcorealchemy.instinct.Instincts;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.instinct.internal.InstinctEffectWrapper;
import targoss.hardcorealchemy.instinct.internal.InstinctSystem;
import targoss.hardcorealchemy.util.MiscVanilla;

/**
 * Tells the player that their active effects have changed.
 * This is a differential packet.
 */
public class MessageInstinctEffects extends MessageToClient {
    public Map<InstinctEffect, InstinctEffectWrapper> effectChanges = new HashMap<>();
    
    public MessageInstinctEffects() {}
    
    public MessageInstinctEffects(Map<InstinctEffect, InstinctEffectWrapper> effectChanges) {
        this.effectChanges.putAll(effectChanges);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(effectChanges.size());
        for (Map.Entry<InstinctEffect, InstinctEffectWrapper> entry : effectChanges.entrySet()) {
            InstinctEffect effect = entry.getKey();
            ByteBufUtils.writeUTF8String(buf, effect.getRegistryName().toString());
            InstinctEffectWrapper wrapper = entry.getValue();
            boolean isDeactivation = wrapper == null;
            buf.writeBoolean(isDeactivation);
            if (!isDeactivation) {
                buf.writeFloat(wrapper.amplifier);
                buf.writeFloat(wrapper.maxInstinct);
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int numEffects = buf.readInt();
        for (int i = 0; i < numEffects; i++) {
            String effectString = ByteBufUtils.readUTF8String(buf);
            InstinctEffect effect = Instincts.EFFECT_REGISTRY.getValue(new ResourceLocation(effectString));
            if (effect == null) {
                throw new NullPointerException("Could not find instinct effect with name '" + effectString + "'");
            }
            boolean isDeactivation = buf.readBoolean();
            if (isDeactivation) {
                effectChanges.put(effect, null);
            }
            else {
                InstinctEffectWrapper wrapper = new InstinctEffectWrapper();
                wrapper.effect = effect;
                wrapper.amplifier = buf.readFloat();
                wrapper.maxInstinct = buf.readFloat();
                effectChanges.put(effect, wrapper);
            }
        }
    }
    
    public static class ReceiveAction implements Runnable {
        private Map<InstinctEffect, InstinctEffectWrapper> effectChanges;
        
        public ReceiveAction(MessageInstinctEffects message) {
            this.effectChanges = message.effectChanges;
        }
        
        @Override
        public void run() {
            EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
            ICapabilityInstinct instinct = player.getCapability(ProviderInstinct.INSTINCT_CAPABILITY, null);
            if (instinct == null) {
                return;
            }
            InstinctSystem.transitionEffects(player, instinct, effectChanges);
        }
    }
    
    public static class Handler implements IMessageHandler<MessageInstinctEffects, IMessage> {
        @Override
        public IMessage onMessage(MessageInstinctEffects message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(new ReceiveAction(message));
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient, IMessage>> getHandlerClass() {
        return Handler.class;
    }

}
