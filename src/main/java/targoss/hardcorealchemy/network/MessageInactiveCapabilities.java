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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import hellfirepvp.astralsorcery.common.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.capability.inactive.IInactiveCapabilities;
import targoss.hardcorealchemy.capability.inactive.IInactiveCapabilities.Cap;
import targoss.hardcorealchemy.util.MiscVanilla;
import targoss.hardcorealchemy.capability.inactive.ProviderInactiveCapabilities;

public class MessageInactiveCapabilities extends MessageToClient {
    
    public MessageInactiveCapabilities() { }
    
    /**
     * Each tag is a key-value pair corresponding to
     * IInactiveCapabilities.getCapabilityMap()
     * (only caps with sync=true are included)
     */
    public NBTTagCompound nbtCapMap = new NBTTagCompound();
    
    // Warning: NOT equivalent to the keys in StorageInactiveCapabilities. Separate concerns!
    private static final String NET_DATA = "data";
    private static final String NET_PERSISTENCE = "persistsOnDeath";
    
    public MessageInactiveCapabilities(IInactiveCapabilities inactives) {
        ConcurrentMap<String, Cap> capMap = inactives.getCapabilityMap();
        for (Map.Entry<String, Cap> capEntry : capMap.entrySet()) {
            Cap cap = capEntry.getValue();
            NBTTagCompound nbtCap = new NBTTagCompound();
            nbtCap.setTag(NET_DATA, cap.data);
            nbtCap.setBoolean(NET_PERSISTENCE, cap.persistsOnDeath);
            nbtCapMap.setTag(capEntry.getKey(), nbtCap);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeNBTTag(buf, nbtCapMap);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        nbtCapMap = ByteBufUtils.readNBTTag(buf);
    }
    
    public static class ReceiveAction implements Runnable {
        private ConcurrentMap<String, Cap> capMap;
        
        public ReceiveAction(NBTTagCompound nbtCapMap) {
            this.capMap = new ConcurrentHashMap<String, Cap>();
            for (String key : nbtCapMap.getKeySet()) {
                Cap cap = new Cap();
                NBTTagCompound capEntry = nbtCapMap.getCompoundTag(key);
                cap.persistsOnDeath = capEntry.getBoolean(NET_PERSISTENCE);
                // Note: Not copied
                cap.data = capEntry.getCompoundTag(NET_DATA);
                this.capMap.put(key, cap);
            }
        }
        
        @Override
        public void run() {
            EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
            IInactiveCapabilities inactives = player.getCapability(ProviderInactiveCapabilities.INACTIVE_CAPABILITIES, null);
            if (inactives == null) {
                return;
            }
            inactives.setCapabilityMap(this.capMap);
        }
    }
    
    public static class Handler implements IMessageHandler<MessageInactiveCapabilities, IMessage> {
        @Override
        public IMessage onMessage(MessageInactiveCapabilities message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(
                    new ReceiveAction(message.nbtCapMap)
                    );
            return null;
        }
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient, IMessage>> getHandlerClass() {
        return Handler.class;
    }

}
