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

import java.util.UUID;

import mchorse.metamorph.Metamorph;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.entitystate.ICapabilityEntityState;
import targoss.hardcorealchemy.capability.humanity.CapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ProviderHumanity;
import targoss.hardcorealchemy.capability.inactive.IInactiveCapabilities;
import targoss.hardcorealchemy.capability.inactive.InactiveCapabilities;
import targoss.hardcorealchemy.capability.inactive.ProviderInactiveCapabilities;
import targoss.hardcorealchemy.capability.instinct.CapabilityInstinct;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.capability.instinct.ProviderInstinct;
import targoss.hardcorealchemy.capability.killcount.ICapabilityKillCount;
import targoss.hardcorealchemy.capability.misc.CapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ProviderMisc;
import targoss.hardcorealchemy.capability.morphstate.ICapabilityMorphState;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.network.MessageHumanity;
import targoss.hardcorealchemy.network.MessageInactiveCapabilities;
import targoss.hardcorealchemy.network.MessageInstinct;
import targoss.hardcorealchemy.network.MessageKillCount;
import targoss.hardcorealchemy.network.MessageMorphState;
import targoss.hardcorealchemy.network.PacketHandler;

/**
 * This listener handles most (but not all) of the lifecycles of capabilities and some attributes.
 * I may have missed some.
 * Certain capabilities are cleared on death, or after death.
 * The capability clearing process is a bit complicated due to
 * Dissolution compat, and the different lifetime needs of various capabilities.
 * Some capabilities are serialized and stored elsewhere, then cleared, due to the player
 * temporarily losing magic ability.
 */
public class ListenerEntityCapabilities extends ConfiguredListener {
    public ListenerEntityCapabilities(Configs configs) {
        super(configs);
    }

    @CapabilityInject(ICapabilityEntityState.class)
    public static final Capability<ICapabilityEntityState> ENTITY_STATE_CAPABILITY = null;
    
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    @CapabilityInject(ICapabilityKillCount.class)
    public static final Capability<ICapabilityKillCount> KILL_COUNT_CAPABILITY = null;
    @CapabilityInject(ICapabilityMorphState.class)
    public static final Capability<ICapabilityMorphState> MORPH_STATE_CAPABILITY = null;
    @CapabilityInject(IInactiveCapabilities.class)
    public static final Capability<IInactiveCapabilities> INACTIVE_CAPABILITIES = null;
    @CapabilityInject(ICapabilityInstinct.class)
    public static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;
    @CapabilityInject(ICapabilityMisc.class)
    public static final Capability<ICapabilityMisc> MISC_CAPABILITY = null;

    @SubscribeEvent
    public void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof EntityPlayer) || (event.getObject() instanceof FakePlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer)(event.getObject());
        {
            ProviderMisc misc = new ProviderMisc();
            misc.instance.setLifetimeUUID(UUID.randomUUID());
            event.addCapability(CapabilityMisc.RESOURCE_LOCATION, misc);
        }
        {
            event.addCapability(InactiveCapabilities.RESOURCE_LOCATION, new ProviderInactiveCapabilities());
        }
        {
            event.addCapability(CapabilityHumanity.RESOURCE_LOCATION, new ProviderHumanity());
            AbstractAttributeMap attributeMap = (player).getAttributeMap();
            if (attributeMap.getAttributeInstance(ICapabilityHumanity.MAX_HUMANITY) == null) {
                attributeMap.registerAttribute(ICapabilityHumanity.MAX_HUMANITY);
            }
        }
        {
            event.addCapability(CapabilityInstinct.RESOURCE_LOCATION, new ProviderInstinct());
            AbstractAttributeMap attributeMap = ((EntityPlayer)event.getObject()).getAttributeMap();
            if (attributeMap.getAttributeInstance(ICapabilityInstinct.MAX_INSTINCT) == null) {
                attributeMap.registerAttribute(ICapabilityInstinct.MAX_INSTINCT);
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        EntityPlayer oldPlayer = event.getOriginal();
        EntityPlayer newPlayer = event.getEntityPlayer();
        if (!event.isWasDeath()) {
            CapUtil.copyOldToNew(MISC_CAPABILITY, oldPlayer, newPlayer);
            CapUtil.copyOldToNew(INSTINCT_CAPABILITY, oldPlayer, newPlayer);
        }
        if (!event.isWasDeath() || Metamorph.keepMorphs.get()) {
            CapUtil.copyOldToNew(KILL_COUNT_CAPABILITY, oldPlayer, newPlayer);
            ListenerPlayerMorphs.updateMaxHumanity(newPlayer);
        }
        // Note: Pruning of individual inactive capabilities only occurs on respawn
        CapUtil.copyOldToNew(INACTIVE_CAPABILITIES, oldPlayer, newPlayer);
    }
    
    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        if (event.player.world.isRemote) {
            return;
        }
        
        syncFullPlayerCapabilities((EntityPlayerMP)(event.player));
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
        ICapabilityHumanity humanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (humanity != null) {
            PacketHandler.INSTANCE.sendTo(new MessageHumanity(humanity, true), (EntityPlayerMP)player);
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
        
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct != null) {
            PacketHandler.INSTANCE.sendTo(new MessageInstinct(instinct), (EntityPlayerMP)player);
        }
    }
}
