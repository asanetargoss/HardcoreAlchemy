/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.entitystate.CapabilityEntityState;
import targoss.hardcorealchemy.capability.entitystate.ProviderEntityState;
import targoss.hardcorealchemy.capability.humanity.CapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ProviderHumanity;
import targoss.hardcorealchemy.capability.inactive.IInactiveCapabilities;
import targoss.hardcorealchemy.capability.inactive.InactiveCapabilities;
import targoss.hardcorealchemy.capability.inactive.ProviderInactiveCapabilities;
import targoss.hardcorealchemy.capability.misc.CapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ProviderMisc;
import targoss.hardcorealchemy.capability.research.CapabilityResearch;
import targoss.hardcorealchemy.capability.research.ICapabilityResearch;
import targoss.hardcorealchemy.capability.research.ProviderResearch;
import targoss.hardcorealchemy.network.MessageInactiveCapabilities;
import targoss.hardcorealchemy.util.MiscVanilla;
import targoss.hardcorealchemy.util.WorldUtil;

/**
 * This listener handles lifecycles of some capabilities.
 * Certain capabilities are cleared on death, or after death.
 * The capability clearing process is a bit complicated due to
 * Dissolution compat, and the different lifetime needs of various capabilities.
 * Some capabilities are serialized and stored elsewhere, then cleared, due to the player
 * temporarily losing magic ability.
 */
public class ListenerEntityCapabilities extends HardcoreAlchemyListener {
    @CapabilityInject(IInactiveCapabilities.class)
    public static final Capability<IInactiveCapabilities> INACTIVE_CAPABILITIES = null;
    @CapabilityInject(ICapabilityMisc.class)
    public static final Capability<ICapabilityMisc> MISC_CAPABILITY = null;
    @CapabilityInject(ICapabilityResearch.class)
    public static final Capability<ICapabilityResearch> RESEARCH_CAPABILITY = null;

    @SubscribeEvent
    public void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)(event.getObject());
            {
                event.addCapability(CapabilityMisc.RESOURCE_LOCATION, new ProviderMisc());
            }
            {
                event.addCapability(InactiveCapabilities.RESOURCE_LOCATION, new ProviderInactiveCapabilities());
            }
            {
                event.addCapability(CapabilityResearch.RESOURCE_LOCATION, new ProviderResearch());
            }
            {
                event.addCapability(CapabilityHumanity.RESOURCE_LOCATION, new ProviderHumanity());
                AbstractAttributeMap attributeMap = (player).getAttributeMap();
                if (attributeMap.getAttributeInstance(ICapabilityHumanity.MAX_HUMANITY) == null) {
                    attributeMap.registerAttribute(ICapabilityHumanity.MAX_HUMANITY);
                }
            }
        }
        if (event.getObject() instanceof EntityLivingBase) {
            event.addCapability(CapabilityEntityState.RESOURCE_LOCATION, new ProviderEntityState());
        }
    }
    
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        EntityPlayer oldPlayer = event.getOriginal();
        EntityPlayer newPlayer = event.getEntityPlayer();
        if (!event.isWasDeath()) {
            CapUtil.copyOldToNew(RESEARCH_CAPABILITY, oldPlayer, newPlayer);
            ListenerPlayerResearch.pruneResearchAfterDeath(newPlayer);
        }
        // Note: Pruning of individual inactive capabilities only occurs on respawn
        CapUtil.copyOldToNew(INACTIVE_CAPABILITIES, oldPlayer, newPlayer);
        CapUtil.copyOldToNew(MISC_CAPABILITY, oldPlayer, newPlayer);
        if (event.isWasDeath()) {
            ICapabilityMisc misc = newPlayer.getCapability(MISC_CAPABILITY, null);
            misc.setLifetimeUUID(UUID.randomUUID());
        }
    }
    
    protected static Map<UUID, UUID> permanentIDToPlayerID = new HashMap<>();
    protected static Map<UUID, UUID> playerIDToPermanentID = new HashMap<>();
    
    public static EntityPlayer getPlayerFromPermanentID(UUID permanentID) {
        UUID playerID = permanentIDToPlayerID.get(permanentID);
        if (playerID == null) {
            return null;
        }
        World world = WorldUtil.getOverworld();
        MinecraftServer server = MiscVanilla.getServer(world);
        if (server == null) {
            return null;
        }
        return server.getPlayerList().getPlayerByUUID(playerID);
    }
    
    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.player.world.isRemote) {
            return;
        }
        
        ICapabilityMisc misc = event.player.getCapability(MISC_CAPABILITY, null);
        if (misc != null) {
            if (misc.getPermanentUUID() == null || misc.getPermanentUUID().equals(new UUID(0, 0))) {
                misc.setPermanentUUID(UUID.randomUUID());
            }
            UUID permanentID = misc.getPermanentUUID();
            permanentIDToPlayerID.put(permanentID, event.player.getUniqueID());
            playerIDToPermanentID.put(event.player.getUniqueID(), permanentID);
        }
        
        syncFullPlayerCapabilities((EntityPlayerMP)(event.player));
    }
    
    @SubscribeEvent
    public void onPlayerLogout(PlayerLoggedOutEvent event) {
        if (event.player.world.isRemote) {
            return;
        }
        
        ICapabilityMisc misc = event.player.getCapability(MISC_CAPABILITY, null);
        if (misc != null) {
            UUID permanentID = misc.getPermanentUUID();
            UUID playerID = permanentIDToPlayerID.get(permanentID);
            if (playerID != null) {
                permanentIDToPlayerID.remove(permanentID);
                playerIDToPermanentID.remove(playerID);
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerRespawnMP(PlayerRespawnEvent event) {
        if (event.player.world.isRemote) {
            return;
        }
        
        syncFullPlayerCapabilities((EntityPlayerMP)(event.player));
    }

    @SubscribeEvent
    public void onPlayerEnterDimension(PlayerChangedDimensionEvent event) {
        if (event.player.world.isRemote) {
            return;
        }
        
        syncFullPlayerCapabilities((EntityPlayerMP)(event.player));
    }
    
    public void syncFullPlayerCapabilities(EntityPlayerMP player) {
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (inactives != null) {
            HardcoreAlchemyCore.proxy.messenger.sendTo(new MessageInactiveCapabilities(inactives), (EntityPlayerMP)player);
        }
    }
}
