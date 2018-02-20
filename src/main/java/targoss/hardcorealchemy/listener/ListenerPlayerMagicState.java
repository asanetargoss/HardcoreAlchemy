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

package targoss.hardcorealchemy.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import am2.api.affinity.Affinity;
import am2.api.extensions.IAffinityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.inactive.IInactiveCapabilities;
import targoss.hardcorealchemy.capability.inactive.InactiveCapabilities;
import targoss.hardcorealchemy.capability.inactive.ProviderInactiveCapabilities;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.util.MorphState;

/**
 * Handles capabilities from other magic mods, ex: make Ars Magica
 * affinities only active when the player is capable of using high magic.
 */
public class ListenerPlayerMagicState extends ConfiguredListener {
    public ListenerPlayerMagicState(Configs configs) {
        super(configs);
    }
    
    @CapabilityInject(IInactiveCapabilities.class)
    private static final Capability<IInactiveCapabilities> INACTIVE_CAPABILITIES = null;
    private static final ResourceLocation INACTIVE_CAPABILITIES_RESOURCE = InactiveCapabilities.RESOURCE_LOCATION;
    
    @CapabilityInject(IAffinityData.class)
    private static final Capability<IAffinityData> AFFINITY_CAPABILITY = null;
    
    @SubscribeEvent
    public void onAttachCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (entity instanceof EntityPlayer) {
            event.addCapability(INACTIVE_CAPABILITIES_RESOURCE, new ProviderInactiveCapabilities());
        }
    }
    
    @SubscribeEvent
    public void onPlayerClone(Clone event) {
        // Clone the capability container itself
        EntityPlayer player = event.getEntityPlayer();
        EntityPlayer playerOld = event.getOriginal();
        boolean success = CapUtil.copyOldToNew(INACTIVE_CAPABILITIES, playerOld, player);
        if (!success) {
            return;
        }
        
        if (event.isWasDeath()) {
            // Remove stored caps not persistent on death
            Map<String, IInactiveCapabilities.Cap> caps =
                    player.getCapability(INACTIVE_CAPABILITIES, null).getCapabilityMap();
            List<String> capKeys = new ArrayList<String>();
            capKeys.addAll(caps.keySet());
            for (String key : capKeys) {
                IInactiveCapabilities.Cap cap = caps.get(key);
                if (!cap.persistsOnDeath) {
                    caps.remove(key);
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onChangeMagicState(PlayerTickEvent event) {
        if (event.phase != Phase.END) {
            return;
        }
        
        EntityPlayer player = event.player;
        if (MorphState.canUseHighMagic(player)) {
            if (ModState.isArsMagicaLoaded) {
                activateAffinities(player);
            }
        }
        else {
            if (ModState.isArsMagicaLoaded) {
                deactivateAffinities(player);
            }
        }
    }
    
    public static final String INACTIVE_AFFINITIES = HardcoreAlchemy.MOD_ID + ":inactive_affinities";
    
    @Optional.Method(modid=ModState.ARS_MAGICA_ID)
    public static void activateAffinities(EntityPlayer player) {
        IAffinityData affinities = player.getCapability(AFFINITY_CAPABILITY, null);
        if (affinities == null) {
            return;
        }
        
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (inactives == null) {
            return;
        }
        
        ConcurrentMap<String, IInactiveCapabilities.Cap> caps = inactives.getCapabilityMap();
        IInactiveCapabilities.Cap affinityCap = caps.get(INACTIVE_AFFINITIES);
        if (affinityCap != null) {
            /* The affinity capability has been stored. Retrieve its
             * value and store it in the player's current capability.
             */
            AFFINITY_CAPABILITY.getStorage().readNBT(AFFINITY_CAPABILITY, affinities, null, affinityCap.data);
            
            caps.remove(INACTIVE_AFFINITIES);
        }
    }
    
    @Optional.Method(modid=ModState.ARS_MAGICA_ID)
    public static void deactivateAffinities(EntityPlayer player) {
        IAffinityData affinities = player.getCapability(AFFINITY_CAPABILITY, null);
        if (affinities == null) {
            return;
        }
        
        IInactiveCapabilities inactives = player.getCapability(INACTIVE_CAPABILITIES, null);
        if (inactives == null) {
            return;
        }
        
        ConcurrentMap<String, IInactiveCapabilities.Cap> caps = inactives.getCapabilityMap();
        IInactiveCapabilities.Cap affinityCap = caps.get(INACTIVE_AFFINITIES);
        if (affinityCap == null) {
            /* The affinity capability is not deactivated yet.
             * Store the existing Ars Magica affinity capability here.
             */
            affinityCap = new IInactiveCapabilities.Cap();
            affinityCap.data = (NBTTagCompound)(AFFINITY_CAPABILITY.getStorage().writeNBT(AFFINITY_CAPABILITY, affinities, null));
            affinityCap.persistsOnDeath = false;
            caps.put(INACTIVE_AFFINITIES, affinityCap);
        }
        
        // Set all affinity depths to zero to prevent magical effects
        for (Affinity affinity : affinities.getAffinities().keySet()) {
            affinities.setAffinityDepth(affinity, 0.0D);
        }
    }
}
