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
import net.minecraft.util.math.BlockPos;
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
        worldHumanity.registerMorphAbilityLocation(event.misc.getLifetimeUUID(), event.player.getUniqueID(), event.pos);
    }
    
    public static boolean doesPhylacteryStillExist(World world, UUID lifetimeUUID, UUID playerUUID) {
        ICapabilityWorldHumanity worldHumanity = UniverseCapabilityManager.INSTANCE.getCapability(world, HUMANITY_WORLD_CAPABILITY);
        if (worldHumanity == null) {
            return true;
        }
        return worldHumanity.getMorphAbilityLocation(lifetimeUUID, playerUUID) != null;
    }
    
    @SubscribeEvent
    public void onPlayerPhylacteryDestroyed(EventHumanityPhylactery.Destroy event) {
        ICapabilityWorldHumanity worldHumanity = UniverseCapabilityManager.INSTANCE.getCapability(event.world, HUMANITY_WORLD_CAPABILITY);
        if (worldHumanity == null) {
            return;
        }
        boolean wasRegistered = worldHumanity.unregisterMorphAbilityLocation(event.lifetimeUUID, event.playerUUID, event.pos);
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
        BlockPos morphAbilityLocation = worldHumanity.getMorphAbilityLocation(misc.getLifetimeUUID(), player.getUniqueID());
        if (morphAbilityLocation != null) {
            // Phylactery exists and is still valid
            return;
        }
        // Phylactery is gone or no longer valid
        MorphState.forceForm(coreConfigs, player, MorphAbilityChangeReason.DESTROYED_HUMAN_FORM_PHYLACTERY);
    }
    
    public void onPlayerDeath() {
        // TODO: Implement (probably as event listener). Remove the morph ability location. If the heart of form is loaded and active, deactivate it.
    }
}
