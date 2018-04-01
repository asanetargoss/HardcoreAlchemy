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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import targoss.hardcorealchemy.capability.morphstate.CapabilityMorphState;
import targoss.hardcorealchemy.capability.morphstate.ICapabilityMorphState;
import targoss.hardcorealchemy.capability.morphstate.ProviderMorphState;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.entity.EntityFishSwarm;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.EntityUtil;

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
                if (player.worldObj.isRemote) {
                    Chat.notifySP(player, new TextComponentTranslation("hardcorealchemy.ability.fishing.endhunt"));
                }
                morphState.setFishingTimer(0);
            }
            else {
                // Fish swarm spawn timer
                int timer = morphState.getFishingTimer();
                
                if (!player.worldObj.isRemote) {
                    if (timer >= FISH_WAIT_PERIOD && (timer-FISH_WAIT_PERIOD) % FISH_SPAWN_INTERVAL == 0) {
                        // Find a spot to spawn a fish swarm entity (or if not, give up)
                        for (int i = 0; i < 15; i++) {
                            // Attempt to spawn swarm between 3 and 7 blocks away (each axis)
                            float min = 3.0F;
                            float max = 7.0F;
                            float range = max-min;
                            float distanceX = (random.nextFloat()-0.5F)*2.0F;
                            distanceX = distanceX>0.0F ? (distanceX*range)+min : (distanceX*range)-min;
                            float distanceY = (random.nextFloat()-0.5F)*2.0F;
                            distanceY = distanceY>0.0F ? (distanceY*range)+min : (distanceY*range)-min;
                            float distanceZ = (random.nextFloat()-0.5F)*2.0F;
                            distanceZ = distanceZ>0.0F ? (distanceZ*range)+min : (distanceZ*range)-min;
                            
                            BlockPos spawnPos = new BlockPos(player.posX+distanceX, player.posY+distanceY, player.posZ+distanceZ);
                            
                            if (player.worldObj.getBlockState(spawnPos).getMaterial() == Material.WATER) {
                                EntityFishSwarm fishSwarm = new EntityFishSwarm(player.worldObj);
                                EntityUtil.createLivingEntityAt(fishSwarm, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                                break;
                            }
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
}
