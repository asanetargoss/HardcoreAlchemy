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

import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import targoss.hardcorealchemy.capability.killcount.ICapabilityKillCount;
import targoss.hardcorealchemy.util.MiscVanilla;

public class MessageKillCount extends MessageToClient {
    
    public MessageKillCount() {}
    
    /**
     * Whether or not the kill count map should be cleared.
     */
    public boolean clearCounts;
    /**
     * Mapping between entity morph names and kill counts.
     * Entity names must be valid (see EntityList).
     */
    public Map<String, Integer> updatedKillCounts;
    
    /**
     * Create custom killCount update packet. String keys in the killCounts map
     * MUST be valid entity names (see EntityList).
     */
    public MessageKillCount(Map<String, Integer> killCounts, boolean clearCounts) {
        this.clearCounts = clearCounts;
        this.updatedKillCounts = killCounts;
    }
    
    /**
     * Create a killCount update packet to match the capability.
     */
    public MessageKillCount(ICapabilityKillCount killCount) {
        this.clearCounts = true;
        this.updatedKillCounts = killCount.getKillCounts();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        clearCounts = buf.readBoolean();
        
        int mapSize = buf.readInt();
        Map<String, Integer> killCounts = new HashMap<>();
        for (int i=0; i<mapSize; i++) {
            int entityId = buf.readInt();
            int killCount = buf.readInt();
            killCounts.put(
                    EntityList.CLASS_TO_NAME.get(EntityList.ID_TO_CLASS.get(entityId)),
                    killCount
                    );
        }
        updatedKillCounts = killCounts;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(clearCounts);
        buf.writeInt(updatedKillCounts.size());
        for (Map.Entry<String, Integer> killCount : updatedKillCounts.entrySet()) {
            buf.writeInt(EntityList.getIDFromString(killCount.getKey()));
            buf.writeInt(killCount.getValue());
        }
    }
    
    public static class ReceiveAction implements Runnable {
        @CapabilityInject(ICapabilityKillCount.class)
        public static final Capability<ICapabilityKillCount> KILL_COUNT_CAPABILITY = null;
        
        private boolean clearCounts;
        private Map<String, Integer> updatedKillCounts;
        public ReceiveAction(boolean clearCounts, Map<String, Integer> updatedKillCounts) {
            this.clearCounts = clearCounts;
            this.updatedKillCounts = updatedKillCounts;
        }
        
        @Override
        public void run() {
            EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
            ICapabilityKillCount killCountCap = player.getCapability(KILL_COUNT_CAPABILITY, null);
            if (killCountCap == null) {
                return;
            }
            
            Map<String, Integer> killCounts = killCountCap.getKillCounts();
            if (this.clearCounts) {
                killCounts.clear();
            }
            for (Map.Entry<String, Integer> killCount : this.updatedKillCounts.entrySet()) {
                killCounts.put(killCount.getKey(), killCount.getValue());
            }
        }
    }
    
    public static class Handler implements IMessageHandler<MessageKillCount, IMessage> {
        @Override
        public IMessage onMessage(MessageKillCount message, MessageContext ctx) {
            message.getThreadListener().addScheduledTask(
                        new ReceiveAction(message.clearCounts, message.updatedKillCounts)
                    );
            return null;
        }
        
    }

    @Override
    public Class<? extends IMessageHandler<? extends MessageToClient, IMessage>> getHandlerClass() {
        return Handler.class;
    }

}
