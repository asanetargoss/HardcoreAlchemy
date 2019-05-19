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

import java.util.Random;

import mchorse.metamorph.api.events.MorphEvent;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.capability.morphstate.CapabilityMorphState;
import targoss.hardcorealchemy.capability.morphstate.ICapabilityMorphState;
import targoss.hardcorealchemy.capability.morphstate.ProviderMorphState;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.entity.EntityFishSwarm;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.MiscVanilla;
import targoss.hardcorealchemy.util.MorphState;
import targoss.hardcorealchemy.util.RandomUtil;

public class ListenerPlayerMorphState extends ConfiguredListener {
    public ListenerPlayerMorphState(Configs configs) {
        super(configs);
    }

    @CapabilityInject(ICapabilityMorphState.class)
    public static final Capability<ICapabilityMorphState> MORPH_STATE_CAPABILITY = null;
    public static final ResourceLocation MORPH_STATE_RESOURCE_LOCATION = CapabilityMorphState.RESOURCE_LOCATION;
    
    private Random random = new Random();
    
    @SubscribeEvent
    public void onAttachCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (entity instanceof EntityPlayer) {
            event.addCapability(MORPH_STATE_RESOURCE_LOCATION, new ProviderMorphState());
        }
    }
    
    @SubscribeEvent
    public void onMorph(MorphEvent.Post event) {
        EntityPlayer player = event.player;
        
        ICapabilityMorphState morphState = player.getCapability(MORPH_STATE_CAPABILITY, null);
        if (morphState == null) {
            return;
        }
        
        // Reset underwater fishing (unless the player just joined the world)
        if (player.ticksExisted > 1) {
            morphState.setFishingTimer(0);
        }
    }
    
    private static final int FISH_WAIT_PERIOD  = 20 * 60; // 60 seconds before first spawn
    private static final int FISH_SPAWN_INTERVAL  = 20 * 10; // 10 seconds between spawns
    
    /**
     * Handles the appearance of fish swarms when the player is
     * in underwater fishing mode.
     */
    @SubscribeEvent
    public void onUnderwaterFishing(PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (player.ticksExisted <= 1) {
            // Prevents the hunt ending on join world
            return;
        }
        
        ICapabilityMorphState morphState = player.getCapability(MORPH_STATE_CAPABILITY, null);
        if (morphState == null) {
            return;
        }
        
        if (morphState.getIsFishingUnderwater()) {
            // Player must be in water to continue the hunt
            if (!player.isInWater()) {
                morphState.setIsFishingUnderwater(false);
                if (player.world.isRemote) {
                    Chat.messageSP(Chat.Type.NOTIFY, player, new TextComponentTranslation("hardcorealchemy.ability.fishing.endhunt"));
                }
                morphState.setFishingTimer(0);
            }
            else {
                // Fish swarm spawn timer
                int timer = morphState.getFishingTimer();
                
                if (!player.world.isRemote) {
                    if (timer >= FISH_WAIT_PERIOD && (timer-FISH_WAIT_PERIOD) % FISH_SPAWN_INTERVAL == 0) {
                        // Find a spot to spawn a fish swarm entity (or if not, give up)
                        int attempts = 15;
                        BlockPos spawnPos = RandomUtil.findSuitableBlockPosInRangeSigned(
                                random, attempts,
                                player.getPosition(), 3.0F, 7.0F,
                                (BlockPos pos) -> player.world.getBlockState(pos).getMaterial() == Material.WATER);
                        if (spawnPos != null) {
                            EntityFishSwarm fishSwarm = new EntityFishSwarm(player.world);
                            EntityUtil.createLivingEntityAt(fishSwarm, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                        }
                    }
                }
                
                timer++;
                if (timer > Integer.MAX_VALUE / 2) {
                    // Prevent overflow
                    timer = (timer - FISH_WAIT_PERIOD) % FISH_SPAWN_INTERVAL + FISH_WAIT_PERIOD;
                }
                morphState.setFishingTimer(timer);
            }
        }
    }
    
    /** When the player has the water_breath ability,
     * invert the effect of the player losing dig speed underwater,
     * so the player instead digs normally when in water and slowly
     * when out of water.
     */
    @SubscribeEvent
    public void onPlayerDigUnderwater(PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!MorphState.hasMorphAbility(player, "water_breath")) {
            return;
        }
        
        float modifier = 1.0F;
        
        boolean inWater = player.isInWater();
        
        // Undo mining penalty when swimming due to feet not touching the ground
        if (inWater && !player.onGround) {
            modifier *= 5.0F;
        }
        
        if (player.isInsideOfMaterial(Material.WATER)) {
            if (!EnchantmentHelper.getAquaAffinityModifier(player)) {
                // Only undo it if Aqua Affinity is not applying its own effect
                modifier *= 5.0F;
            }
        }
        else {
            // Be a bit more lenient. Allow the player to dig blocks easily as long as they are breathing water.
            if (!inWater) {
                modifier *= 0.2F;
            }
        }
        
        event.setNewSpeed(event.getNewSpeed() * modifier);
    }
    
    protected static final int MAX_WATER_TIME = 20 * 4;
    protected static int waterTime = 0;
    protected static boolean highWaterTime = false;
    
    /**
     * Increase FOV when the player is in water and has the swim ability, to combat
     * motion sickness due to fast movement.
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onSwimmingMorphInWater(FOVUpdateEvent event) {
        EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
        if (!MorphState.hasMorphAbility(player, "swim")) {
            return;
        }
        
        if (waterTime > MAX_WATER_TIME * 0.5) {
            highWaterTime = true;
        }
        else if (waterTime <= 0) {
            highWaterTime = false;
        }
        
        if (highWaterTime) {
            event.setNewfov(event.getNewfov() * 4.0f);
        }
        
        if (player.isInWater()) {
            if (waterTime < MAX_WATER_TIME) { 
                waterTime++;
            }   
        }
        else {
            if (waterTime > 0) {
                waterTime--;
            }
        }
    }
    
}
