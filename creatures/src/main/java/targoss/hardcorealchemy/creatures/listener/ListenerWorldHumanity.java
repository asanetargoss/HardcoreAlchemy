/*
 * Copyright 2017-2022 asanetargoss
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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
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
import targoss.hardcorealchemy.capability.worldhumanity.CapabilityWorldHumanity;
import targoss.hardcorealchemy.capability.worldhumanity.ICapabilityWorldHumanity;
import targoss.hardcorealchemy.capability.worldhumanity.ProviderWorldHumanity;
import targoss.hardcorealchemy.creatures.event.EventHumanityPhylactery;
import targoss.hardcorealchemy.creatures.util.MorphState;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
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
        ICapabilityWorldHumanity worldHumanity = UniverseCapabilityManager.INSTANCE.getCapability(event.player.world, HUMANITY_WORLD_CAPABILITY);
        if (worldHumanity == null) {
            return;
        }
        ICapabilityHumanity humanity = event.player.getCapability(HUMANITY_CAPABILITY, null);
        if (humanity == null) {
            return;
        }
        if (humanity.getHasForgottenMorphAbility()) {
            return;
        }
        MorphState.forceForm(HardcoreAlchemyCore.proxy.configs, event.player, MorphAbilityChangeReason.CREATED_HUMAN_FORM_PHYLACTERY, event.morphTarget);
        worldHumanity.registerPhylactery(event.misc.getLifetimeUUID(), event.player.getUniqueID(), event.pos, event.dimension);
    }
    
    public static boolean doesPlayerPhylacteryStillExist(World world, UUID lifetimeUUID, UUID playerUUID) {
        ICapabilityWorldHumanity worldHumanity = UniverseCapabilityManager.INSTANCE.getCapability(world, HUMANITY_WORLD_CAPABILITY);
        if (worldHumanity == null) {
            return true;
        }
        return worldHumanity.hasPlayerPhylactery(lifetimeUUID, playerUUID);
    }
    
    @SubscribeEvent
    public void onPlayerPhylacteryDestroyed(EventHumanityPhylactery.Destroy event) {
        ICapabilityWorldHumanity worldHumanity = UniverseCapabilityManager.INSTANCE.getCapability(event.world, HUMANITY_WORLD_CAPABILITY);
        if (worldHumanity == null) {
            return;
        }
        boolean wasRegistered = worldHumanity.unregisterPhylactery(event.lifetimeUUID, event.playerUUID, event.pos, event.dimension);
        if (!wasRegistered) {
            return;
        }
        MinecraftServer server = MiscVanilla.getServer(event.world);
        if (server == null) {
            return;
        }
        EntityPlayer player = server.getPlayerList().getPlayerByUUID(event.playerUUID);
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
        
        ICapabilityWorldHumanity worldHumanity = UniverseCapabilityManager.INSTANCE.getCapability(player.world, HUMANITY_WORLD_CAPABILITY);
        if (worldHumanity == null) {
            return;
        }
        ICapabilityMisc misc = player.getCapability(MISC_CAPABILITY, null);
        if (misc == null) {
            return;
        }
        if (worldHumanity.hasPlayerPhylactery(misc.getLifetimeUUID(), player.getUniqueID())) {
            // Phylactery exists and is still valid
            return;
        }
        // Phylactery is gone or no longer valid
        MorphState.forceForm(coreConfigs, player, MorphAbilityChangeReason.DESTROYED_HUMAN_FORM_PHYLACTERY);
    }
    
    // TODO: If the player is perma-morphed, their phylactery should deactivate:
    //       [x] Add per-entry enum PhylacteryState, with choices of active (default), dormant, and deactivated
    //       [ ] Add function to set the PhylacteryState, in the universe cap and also the tile if it is loaded
    //       [ ] When the tile is loaded, check PhylacteryState in the universe cap
    //       [ ] Change phylactery behavior based on PhylacteryState
    
    public static void onPlayerDeath(EntityPlayer oldPlayer, EntityPlayer newPlayer, boolean keepPhylactery) {
        ICapabilityHumanity oldHumanity = oldPlayer.getCapability(HUMANITY_CAPABILITY, null);
        if (oldHumanity == null) { return; }
        ICapabilityHumanity newHumanity = newPlayer.getCapability(HUMANITY_CAPABILITY, null);
        if (newHumanity == null) { return; }
        ICapabilityWorldHumanity worldHumanity = UniverseCapabilityManager.INSTANCE.getCapability(newPlayer.world, HUMANITY_WORLD_CAPABILITY);
        
        boolean hadPhylactery = oldHumanity.getIsHumanFormInPhylactery();
        
        if (keepPhylactery) {
            // If available, mark previous phylactery as REINCARNATED
            ICapabilityMisc misc = oldPlayer.getCapability(MISC_CAPABILITY, null);
            if (worldHumanity != null && misc != null) {
                ICapabilityWorldHumanity.Phylactery oldPhylactery = worldHumanity.getPlayerPhylactery(misc.getLifetimeUUID(), oldPlayer.getUniqueID());
                if (oldPhylactery != null) {
                    oldPhylactery.data.state = ICapabilityWorldHumanity.State.REINCARNATED;
                    // TODO: Update the associated tile entity as needed, if it is loaded
                }
            }
            // getPhylacteryState
            // TODO: Add a duplicate entry. Same position but different IDs. Mark previous as REINCARNATED. Update the tile entity as needed to reference the new entry
        }
        else {
            newHumanity.setIsHumanFormInPhylactery(false);
            // TODO: Mark the entry as "dormant", but don't remove it
            // TODO: If the chunk is loaded, mark any affected phylactery as dormant, which means the phylactery is technically still active, but it has no flame and can't be doused with water.
            // TODO: When a phylactery is loaded, load dormancy state from the world capability
        }
        // TODO: Implement (probably as event listener, PlayerEvent.Clone? PlayerRespawnEvent?). Remove the morph ability location. If the phylactery is loaded and active, deactivate it.
        
        // TODO: What to do about keepPhylactery (i.e. keepMorphs)? It seems we may need a new "unique ID" to preserve morphs across lives. OR... need to transfer the phylactery ownership to the new ID. Maybe create a duplicate entry?
    }
}
