/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.network;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ProviderMisc;
import targoss.hardcorealchemy.network.RequestToServer;
import targoss.hardcorealchemy.tweaks.HardcoreAlchemyTweaks;

public class RequestCraftItemTimefrozen extends RequestToServer<HardcoreAlchemyTweaks> {
    public static final byte MAX_ITEM_MODEL_PROPERTIES = 24;
    public static final int MAX_ITEM_MODEL_PROPERTY_KEY_LENGTH = 64;
    public static final String KEY_TOO_LONG = "too_long_";
    public @Nullable Map<String, Float> itemModelProperties = new HashMap<>();
    
    public void clear() {
        itemModelProperties.clear();
    }
    
    public RequestCraftItemTimefrozen() {}
    
    public RequestCraftItemTimefrozen(Map<String, Float> itemModelProperties) {
        this.itemModelProperties = itemModelProperties;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        final byte numProperties = itemModelProperties == null ? 0 : (byte)Math.min(MAX_ITEM_MODEL_PROPERTIES, itemModelProperties.size());
        buf.writeByte(numProperties);
        if (numProperties > 0) {
            byte propertiesSerialized = 0;
            for (Map.Entry<String, Float> entry : itemModelProperties.entrySet()) {
                if (propertiesSerialized >= numProperties) {
                    break;
                }
                ++propertiesSerialized;
                String key = entry.getKey();
                if (key.length() > MAX_ITEM_MODEL_PROPERTY_KEY_LENGTH) {
                    // Leave a paper trail
                    key = (KEY_TOO_LONG + key).substring(0, MAX_ITEM_MODEL_PROPERTY_KEY_LENGTH);
                }
                ByteBufUtils.writeUTF8String(buf, key);
                float value = entry.getValue();
                buf.writeFloat(value);
            }
        }
    }
    
    /**
     * Checks the string length of buf but does not advance the reader index.
     */
    protected static int checkStringLength(ByteBuf buf) {
        buf.markReaderIndex();
        int length = ByteBufUtils.readVarInt(buf, 2);
        buf.resetReaderIndex();
        return length;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        byte numProperties = buf.readByte();
        if (numProperties > MAX_ITEM_MODEL_PROPERTIES) {
            return;
        }
        if (numProperties > 0) {
            for (byte i = 0; i < numProperties; ++i) {
                int keyLength = checkStringLength(buf);
                if (keyLength > MAX_ITEM_MODEL_PROPERTY_KEY_LENGTH) {
                    // Ignore the whole contents of the message so
                    // the handler is called with empty data.
                    clear();
                    return;
                }
                String key = ByteBufUtils.readUTF8String(buf);
                float value = buf.readFloat();
                itemModelProperties.put(key, value);
            }
        }
    }
    
    public static class ReceiveAction implements Runnable {
        private EntityPlayer player;
        private Map<String, Float> itemModelProperties;
        
        public ReceiveAction(EntityPlayer player, Map<String, Float> itemModelProperties) {
            this.player = player;
            this.itemModelProperties = itemModelProperties;
        }

        @Override
        public void run() {
            ICapabilityMisc misc = player.getCapability(ProviderMisc.MISC_CAPABILITY, null);
            if (misc != null) {
                misc.setEnqueuedItemModelProperties(itemModelProperties);
            }
        }
        
    }
    
    public static class Handler implements IMessageHandler<RequestCraftItemTimefrozen, IMessage> {
        @Override
        public IMessage onMessage(RequestCraftItemTimefrozen message, MessageContext ctx) {
            message.getThreadListener(ctx).addScheduledTask(new ReceiveAction(
                    ctx.getServerHandler().playerEntity,
                    message.itemModelProperties
                ));
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends RequestToServer<HardcoreAlchemyTweaks>, IMessage>> getHandlerClass() {
        return Handler.class;
    }

}
