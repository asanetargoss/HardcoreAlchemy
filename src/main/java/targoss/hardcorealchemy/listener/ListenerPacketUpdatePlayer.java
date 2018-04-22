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

package targoss.hardcorealchemy.listener;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.inactive.IInactiveCapabilities;
import targoss.hardcorealchemy.capability.killcount.ICapabilityKillCount;
import targoss.hardcorealchemy.capability.morphstate.ICapabilityMorphState;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.network.MessageHumanity;
import targoss.hardcorealchemy.network.MessageInactiveCapabilities;
import targoss.hardcorealchemy.network.MessageKillCount;
import targoss.hardcorealchemy.network.MessageMorphState;
import targoss.hardcorealchemy.network.PacketHandler;

/**
 * It is convention in this mod to not create capabilities or do any capability
 * calculations on the client side. This event listener is responsible for sending
 * the little data that the client needs to function correctly.
 */
public class ListenerPacketUpdatePlayer extends ConfiguredListener {
    public ListenerPacketUpdatePlayer(Configs configs) {
        super(configs);
    }
    
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    @CapabilityInject(ICapabilityKillCount.class)
    public static final Capability<ICapabilityKillCount> KILL_COUNT_CAPABILITY = null;
    @CapabilityInject(ICapabilityMorphState.class)
    public static final Capability<ICapabilityMorphState> MORPH_STATE_CAPABILITY = null;
    @CapabilityInject(IInactiveCapabilities.class)
    public static final Capability<IInactiveCapabilities> INACTIVE_CAPABILITIES = null;
    
    private static final int PLAYER_UPDATE_TICKS = 7;
    
    /**
     * Send packets to player about their capabilities
     */
    @SubscribeEvent
    public void onPlayerJoinMP(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof EntityPlayer)) {
            return;
        }
        
        EntityPlayer player = (EntityPlayer)(event.getEntity());
        if (player.world.isRemote) {
            return;
        }
        
        syncFullCapabilities((EntityPlayerMP)player);
    }
    
    @SubscribeEvent
    public void onPlayerRespawnMP(PlayerRespawnEvent event) {
        if (event.player.world.isRemote) {
            return;
        }
        
        syncFullCapabilities((EntityPlayerMP)(event.player));
    }
    
    public void syncFullCapabilities(EntityPlayerMP player) {
        ICapabilityHumanity humanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (humanity != null) {
            PacketHandler.INSTANCE.sendTo(new MessageHumanity(humanity), (EntityPlayerMP)player);
        }
        
        ICapabilityKillCount killCount = player.getCapability(KILL_COUNT_CAPABILITY, null);
        if (killCount != null) {
            PacketHandler.INSTANCE.sendTo(new MessageKillCount(killCount), (EntityPlayerMP)player);
        }
        
        ICapabilityMorphState morphState = player.getCapability(MORPH_STATE_CAPABILITY, null);
        if (morphState != null) {
            PacketHandler.INSTANCE.sendTo(new MessageMorphState(morphState), (EntityPlayerMP)player);
        }
        
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (morphState != null) {
            PacketHandler.INSTANCE.sendTo(new MessageInactiveCapabilities(inactives), (EntityPlayerMP)player);
        }
    }
}
