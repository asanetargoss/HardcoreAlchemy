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

package targoss.hardcorealchemy.creatures.listener;

import mchorse.metamorph.Metamorph;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.VirtualCapabilityManager;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;
import targoss.hardcorealchemy.creatures.capability.instinct.CapabilityInstinct;
import targoss.hardcorealchemy.creatures.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.creatures.capability.instinct.ProviderInstinct;
import targoss.hardcorealchemy.creatures.capability.instinct.StorageInstinct;
import targoss.hardcorealchemy.creatures.capability.killcount.CapabilityKillCount;
import targoss.hardcorealchemy.creatures.capability.killcount.ICapabilityKillCount;
import targoss.hardcorealchemy.creatures.capability.killcount.StorageKillCount;
import targoss.hardcorealchemy.creatures.capability.morphstate.CapabilityMorphState;
import targoss.hardcorealchemy.creatures.capability.morphstate.ICapabilityMorphState;
import targoss.hardcorealchemy.creatures.capability.morphstate.StorageMorphState;
import targoss.hardcorealchemy.creatures.capability.worldhumanity.CapabilityWorldHumanity;
import targoss.hardcorealchemy.creatures.capability.worldhumanity.ICapabilityWorldHumanity;
import targoss.hardcorealchemy.creatures.capability.worldhumanity.StorageWorldHumanity;
import targoss.hardcorealchemy.creatures.network.MessageHumanity;
import targoss.hardcorealchemy.creatures.network.MessageInstinct;
import targoss.hardcorealchemy.creatures.network.MessageKillCount;
import targoss.hardcorealchemy.creatures.network.MessageMorphState;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;

public class ListenerCapabilities extends HardcoreAlchemyListener {
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    @CapabilityInject(ICapabilityKillCount.class)
    public static final Capability<ICapabilityKillCount> KILL_COUNT_CAPABILITY = null;
    @CapabilityInject(ICapabilityInstinct.class)
    public static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;
    @CapabilityInject(ICapabilityMorphState.class)
    public static final Capability<ICapabilityMorphState> MORPH_STATE_CAPABILITY = null;
    
    @Override
    public void registerCapabilities(CapabilityManager manager, VirtualCapabilityManager virtualManager) {
        manager.register(ICapabilityKillCount.class, new StorageKillCount(), CapabilityKillCount.class);
        manager.register(ICapabilityInstinct.class, new StorageInstinct(), CapabilityInstinct.class);
        manager.register(ICapabilityMorphState.class, new StorageMorphState(), CapabilityMorphState.class);
        manager.register(ICapabilityWorldHumanity.class, new StorageWorldHumanity(), CapabilityWorldHumanity.class);
    }

    @SubscribeEvent
    public void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof EntityPlayer) {
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
            CapUtil.copyOldToNew(INSTINCT_CAPABILITY, oldPlayer, newPlayer);
        }
        if (!event.isWasDeath() || Metamorph.keepMorphs.get()) {
            CapUtil.copyOldToNew(KILL_COUNT_CAPABILITY, oldPlayer, newPlayer);
            CapUtil.copyOldToNew(HUMANITY_CAPABILITY, oldPlayer, newPlayer);
            ListenerPlayerMorphs.updateMaxHumanity(newPlayer, false);
        }
        if (event.isWasDeath()) {
            boolean keepPhylactery = Metamorph.keepMorphs.get();
            ListenerWorldHumanity.onPlayerDeath(oldPlayer, newPlayer, keepPhylactery);
        }
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
            HardcoreAlchemyCreatures.proxy.messenger.sendTo(new MessageHumanity(humanity, true), (EntityPlayerMP)player);
        }
        ICapabilityKillCount killCount = player.getCapability(KILL_COUNT_CAPABILITY, null);
        if (killCount != null) {
            HardcoreAlchemyCreatures.proxy.messenger.sendTo(new MessageKillCount(killCount), (EntityPlayerMP)player);
        }
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct != null) {
            HardcoreAlchemyCreatures.proxy.messenger.sendTo(new MessageInstinct(instinct), (EntityPlayerMP)player);
        }
        ICapabilityMorphState morphState = player.getCapability(MORPH_STATE_CAPABILITY, null);
        if (morphState != null) {
            HardcoreAlchemyCreatures.proxy.messenger.sendTo(new MessageMorphState(morphState), (EntityPlayerMP)player);
        }
    }
}
