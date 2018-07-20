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

import java.util.Map;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.capability.instincts.ICapabilityInstinct;
import targoss.hardcorealchemy.capability.instincts.ICapabilityInstinct.InstinctEntry;
import targoss.hardcorealchemy.capability.instincts.ProviderInstinct;
import targoss.hardcorealchemy.instinct.Instincts;
import targoss.hardcorealchemy.util.MiscVanilla;

public class MessageInstinct extends MessageToClient implements Runnable {
    
    public MessageInstinct() { }
    
    public float instinct;
    public ResourceLocation activeInstinct;
    public InstinctEntry[] instinctEntries;
    
    public MessageInstinct(ICapabilityInstinct instinct) {
        this.instinct = instinct.getInstinct();
        this.activeInstinct = instinct.getActiveInstinctId();
        instinctEntries = instinct.getInstinctMap().values().toArray(new InstinctEntry[]{});
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(instinct);
        
        if (activeInstinct != null) {
            buf.writeBoolean(true);
            ByteBufUtils.writeUTF8String(buf, activeInstinct.toString());
        }
        else {
            buf.writeBoolean(false);
        }
        
        buf.writeInt(instinctEntries.length);
        for (InstinctEntry entry : instinctEntries) {
            // No need for integer ids. Maybe 5 instincts to send at most.
            ByteBufUtils.writeUTF8String(buf, entry.id.toString());
            ByteBufUtils.writeTag(buf, entry.instinct.serializeNBT());
            buf.writeFloat(entry.weight);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.instinct = buf.readFloat();
        
        if (buf.readBoolean() == true) {
            this.activeInstinct = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
        }
        else {
            this.activeInstinct = null;
        }
        
        int instinctCount = buf.readInt();
        this.instinctEntries = new InstinctEntry[instinctCount];
        for (int i = 0; i < instinctCount; i++) {
            InstinctEntry entry = new InstinctEntry();
            boolean invalid = false;
            
            entry.id = new ResourceLocation(ByteBufUtils.readUTF8String(buf));
            
            entry.instinct = Instincts.REGISTRY.getValue(entry.id).createInstinct();
            if (entry.instinct == null) {
                invalid = true;
            }
            entry.instinct.deserializeNBT(ByteBufUtils.readTag(buf));
            
            entry.weight = buf.readFloat();
            
            if (invalid) {
                entry = null;
            }
            this.instinctEntries[i] = entry;
        }
    }
    
    @Override
    public void run() {
        ICapabilityInstinct instinct = MiscVanilla
                .getTheMinecraftPlayer()
                .getCapability(ProviderInstinct.INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        
        instinct.setInstinct(this.instinct);
        Map<ResourceLocation, InstinctEntry> instinctMap = instinct.getInstinctMap();
        instinctMap.clear();
        for (InstinctEntry entry : instinctEntries) {
            instinctMap.put(entry.id, entry);
        }
        
        instinct.setActiveInstinctId(this.activeInstinct);
    }
    
    public static class Handler implements IMessageHandler<MessageInstinct, IMessage> {
        @Override
        public IMessage onMessage(MessageInstinct message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(message);
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient, IMessage>> getHandlerClass() {
        return Handler.class;
    }
}
