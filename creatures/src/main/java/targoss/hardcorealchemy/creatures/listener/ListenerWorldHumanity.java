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

import java.util.UUID;

import javax.annotation.Nullable;

import mchorse.metamorph.api.MorphAPI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.capability.EventUniverseCapabilities;
import targoss.hardcorealchemy.capability.UniverseCapabilityManager;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.MorphAbilityChangeReason;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.creatures.block.TileHumanityPhylactery;
import targoss.hardcorealchemy.creatures.capability.worldhumanity.CapabilityWorldHumanity;
import targoss.hardcorealchemy.creatures.capability.worldhumanity.ICapabilityWorldHumanity;
import targoss.hardcorealchemy.creatures.capability.worldhumanity.ProviderWorldHumanity;
import targoss.hardcorealchemy.creatures.event.EventHumanityPhylactery;
import targoss.hardcorealchemy.creatures.util.MorphState;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.listener.ListenerEntityCapabilities;
import targoss.hardcorealchemy.util.MiscVanilla;

public class ListenerWorldHumanity extends HardcoreAlchemyListener {
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    @CapabilityInject(ICapabilityWorldHumanity.class)
    public static final Capability<ICapabilityWorldHumanity> HUMANITY_WORLD_CAPABILITY = null;
    @CapabilityInject(ICapabilityMisc.class)
    public static final Capability<ICapabilityMisc> MISC_CAPABILITY = null;
    
    @SubscribeEvent
    public void onAttachUniverseCapabilities(EventUniverseCapabilities event) {
        event.addCapability(CapabilityWorldHumanity.RESOURCE_LOCATION, new ProviderWorldHumanity());
    }

    @SubscribeEvent
    public void onPlayerPhylacteryCreated(EventHumanityPhylactery.Create event) {
        if (event.world.isRemote) {
            return;
        }
        if (event.player == null) {
            return;
        }
        ICapabilityHumanity humanity = event.player.getCapability(HUMANITY_CAPABILITY, null);
        if (humanity == null) {
            return;
        }
        if (humanity.getHasForgottenMorphAbility()) {
            return;
        }
        if (event.morphTarget != null) {
            MorphAPI.acquire(event.player, event.morphTarget);
        }
        MorphState.forceForm(HardcoreAlchemyCore.proxy.configs, event.player, MorphAbilityChangeReason.CREATED_HUMAN_FORM_PHYLACTERY, event.morphTarget);
    }

    @SubscribeEvent
    public void onPlayerPhylacteryRecreated(EventHumanityPhylactery.Recreate event) {
        if (!MiscVanilla.isLikelyServer()) {
            return;
        }
        ICapabilityWorldHumanity worldHumanity = UniverseCapabilityManager.INSTANCE.getCapability(HUMANITY_WORLD_CAPABILITY);
        if (worldHumanity == null) {
            return;
        }
        EntityPlayer player = ListenerEntityCapabilities.getPlayerFromPermanentID(event.permanentUUID);
        if (player == null) {
            return;
        }
        ICapabilityHumanity humanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (humanity == null) {
            return;
        }
        if (humanity.getHasForgottenMorphAbility()) {
            return;
        }
        MorphState.forceForm(HardcoreAlchemyCore.proxy.configs, player, MorphAbilityChangeReason.CREATED_HUMAN_FORM_PHYLACTERY, event.morphTarget);
    }
    
    public static boolean doesPlayerPhylacteryStillExist(UUID lifetimeUUID, UUID playerUUID) {
        ICapabilityWorldHumanity worldHumanity = UniverseCapabilityManager.INSTANCE.getCapability(HUMANITY_WORLD_CAPABILITY);
        if (worldHumanity == null) {
            return true;
        }
        return worldHumanity.hasPlayerPhylactery(lifetimeUUID, playerUUID);
    }

    public static boolean doesBlockPhylacteryStillExist(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension) {
        ICapabilityWorldHumanity worldHumanity = UniverseCapabilityManager.INSTANCE.getCapability(HUMANITY_WORLD_CAPABILITY);
        if (worldHumanity == null) {
            return true;
        }
        return worldHumanity.hasBlockPhylactery(lifetimeUUID, playerUUID, pos, dimension);
    }

    public static @Nullable ICapabilityWorldHumanity.Phylactery getBlockPhylactery(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension) {
        ICapabilityWorldHumanity worldHumanity = UniverseCapabilityManager.INSTANCE.getCapability(HUMANITY_WORLD_CAPABILITY);
        if (worldHumanity == null) {
            return null;
        }
        return worldHumanity.getBlockPhylactery(lifetimeUUID, playerUUID, pos, dimension);
    }
    
    @SubscribeEvent
    public void onPlayerPhylacteryDestroyed(EventHumanityPhylactery.Destroy event) {
        EntityPlayer player = ListenerEntityCapabilities.getPlayerFromPermanentID(event.permanentUUID);
        if (player == null) {
            return;
        }
        MorphState.forceForm(coreConfigs, player, MorphAbilityChangeReason.DESTROYED_HUMAN_FORM_PHYLACTERY);
    }
    
    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        ICapabilityHumanity humanity = event.player.getCapability(HUMANITY_CAPABILITY, null);
        if (humanity == null) {
            return;
        }
        if (humanity.getIsHumanFormInPhylactery()) {
            // Nothing to check
            return;
        }
        
        ICapabilityWorldHumanity worldHumanity = UniverseCapabilityManager.INSTANCE.getCapability(HUMANITY_WORLD_CAPABILITY);
        if (worldHumanity == null) {
            return;
        }
        ICapabilityMisc misc = player.getCapability(MISC_CAPABILITY, null);
        if (misc == null) {
            return;
        }
        if (worldHumanity.hasPlayerPhylactery(misc.getLifetimeUUID(), misc.getPermanentUUID())) {
            // Phylactery exists and is still valid
            return;
        }
        // Phylactery is gone or no longer valid
        MorphState.forceForm(coreConfigs, player, MorphAbilityChangeReason.DESTROYED_HUMAN_FORM_PHYLACTERY);
    }
    
    public static void onPlayerDeath(EntityPlayer oldPlayer, EntityPlayer newPlayer, boolean keepPhylactery) {
        if (newPlayer.world.isRemote) {
            return;
        }
        
        ICapabilityHumanity oldHumanity = oldPlayer.getCapability(HUMANITY_CAPABILITY, null);
        if (oldHumanity == null) { return; }
        ICapabilityHumanity newHumanity = newPlayer.getCapability(HUMANITY_CAPABILITY, null);
        if (newHumanity == null) { return; }
        ICapabilityMisc oldMisc = oldPlayer.getCapability(MISC_CAPABILITY, null);
        if (oldMisc == null) { return; }
        ICapabilityMisc newMisc = newPlayer.getCapability(MISC_CAPABILITY, null);
        if (newMisc == null) { return; }
        ICapabilityWorldHumanity worldHumanity = UniverseCapabilityManager.INSTANCE.getCapability(HUMANITY_WORLD_CAPABILITY);
        if (worldHumanity == null) { return; }
        
        ICapabilityWorldHumanity.Phylactery oldPhylactery = worldHumanity.getPlayerPhylactery(oldMisc.getLifetimeUUID(), oldMisc.getPermanentUUID());
        
        // Update the world bookkeeping of the phylactery
        if (keepPhylactery) {
            // If available, mark previous phylactery as REINCARNATED
            // Then add a duplicate entry. Same position but different IDs
            if (oldPhylactery != null) {
                oldPhylactery.state = ICapabilityWorldHumanity.State.REINCARNATED;
            }
            worldHumanity.registerPhylactery(newMisc.getLifetimeUUID(), newMisc.getPermanentUUID(), oldPhylactery.pos, oldPhylactery.dimension, oldPhylactery.morphTarget);
        }
        else {
            // Mark the entry as "dormant", but don't remove it
            // Make the corresponding tile dormant, if it exists
            newHumanity.setIsHumanFormInPhylactery(false);
            if (oldPhylactery != null) {
                oldPhylactery.state = ICapabilityWorldHumanity.State.DORMANT;
            }
        }

        if (oldPhylactery != null) {
            // Update the phylactery's associated tile entity to match the new bookkeeping state, if it is currently loaded.
            // This function may then fire events, which may then change the player's morph state
            TileHumanityPhylactery.checkWorldState(null, oldPhylactery, newMisc.getLifetimeUUID(), newMisc.getPermanentUUID());
        }
    }
}
