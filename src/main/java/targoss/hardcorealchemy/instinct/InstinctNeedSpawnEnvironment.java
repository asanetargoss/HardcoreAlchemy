/*
 * Copyright 2019 asanetargoss
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

package targoss.hardcorealchemy.instinct;

import java.util.ArrayList;
import java.util.List;

import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.EntityMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.instinct.api.IInstinctState;
import targoss.hardcorealchemy.instinct.api.IInstinctState.NeedStatus;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.MiscVanilla;

/**
 * A general purpose instinct need class which determines if a player
 * feels "at home" based on the corresponding morph entity's spawn environment.
 * This instinct need checks the spawn conditions each tick and updates a running average
 * to figure out if the environment is favorable.
 * 
 * If the player's inhuman form feels "at home," the player's instinct recovers normally.
 * If they do not feel "at home," the player's instinct decreases slowly.
 * This instinct need may display an additional message when the player transitions to a favorable environment.
 */
public class InstinctNeedSpawnEnvironment extends InstinctNeedEnvironment {
    
    @CapabilityInject(IMorphing.class)
    private static final Capability<IMorphing> MORPHING_CAPABILITY = null;
    
    // TODO: Consider rolling this into the base class, or have the simpler hysteresis pulled into another implementation
    // TODO: Serialization (+ Networking?)
    protected static final int MIN_HISTORY_CAPACITY = 10 * 20;
    protected static final int MAX_HISTORY_CAPACITY = 4096;
    /**
     * Math.max(MIN_HISTORY_CAPACITY, MAX_HISTORY_CAPACITY - maxAtHomeStreak)
     */
    protected int historyCapacity = MAX_HISTORY_CAPACITY;
    /**
     * In case some entities have extremely rare spawn success,
     * allow preferredAtHomeFrequencies to decay if maxAtHomeStreak
     * never exceeds this value.
     * Decay will only occur when the player is not feeling at home.
     */
    protected static final int MAX_FREQUENCY_ALLOWING_DECAY = 5;
    /**
     * Per tick.
     * In the case of extremely low spawn success, the preferred frequency will
     * decay by one point per hour
     */
    protected static final float PREFERRED_FREQUENCY_DECAY_RATE = 1.0F / (20.0F * 60.0F * 60.0F);
    /**
     * The maximum number of times to queue displaying a message to the player that they feel at home,
     * at the moment when the environment is definitively favorable
     */
    protected static final int MAX_FEEL_AT_HOME_MESSAGE_QUEUE = 3;
    
    protected EntityLivingBase spawnCheckEntity = null;
    protected boolean feelsAtHome = false;
    protected int feelAtHomeMessageQueue = 0;
    protected boolean shouldRandomlyDisplayFeelAtHomeMessage = true;
    protected int atHomeStreak = 0;
    protected int maxAtHomeStreak = 0;
    protected float averageAtHomeFrequency = 0.0F;
    /**
     * The preferred number of ticks (relative to the environment history size) at which the
     * player starts to feel at home. When above this value, the player will not feel negative effects.
     * The value of this will be no greater than maxAtHomeFrequency / 2
     */
    protected float preferredAtHomeFrequency = 0.0F;
    
    // TODO: Dynamically determine if a mob can have this instinct by looping over all biomes and making sure the mob spawns in at least one of them
    
    public InstinctNeedSpawnEnvironment(EntityLivingBase morphEntity) {
        super(morphEntity);
    }

    @Override
    public IInstinctNeed createInstanceFromMorphEntity(EntityLivingBase morphEntity) {
        return new InstinctNeedSpawnEnvironment(morphEntity);
    }

    @Override
    public boolean doesPlayerFeelAtHome(EntityPlayer player) {
        IMorphing morphing = player.getCapability(MORPHING_CAPABILITY, null);
        if (morphing == null) {
            return true;
        }
        
        AbstractMorph morph = morphing.getCurrentMorph();
        if (morph == null || !(morph instanceof EntityMorph)) {
            return true;
        }
        World world = player.world;
        EntityLivingBase morphEntity = ((EntityMorph)morph).getEntity(world);
        BlockPos pos = player.getPosition();
        Biome biome = world.getBiome(pos);
        
        // Adapted from vanilla spawn mechanics, with a few simplifications
        if (morphEntity instanceof EntityLiving && EntityUtil.canEntitySpawnHere((EntityLiving)morphEntity, world, pos)) {
            // World gen surface chunk spawning criteria
            // Some mobs, like Rabbits, only spawn on worldgen
            if (EnumCreatureType.CREATURE.getCreatureClass().isAssignableFrom(morphEntity.getClass()) &&
                    world.getTopSolidOrLiquidBlock(pos).getY() < pos.getY() &&
                    !morphEntity.isOffsetPositionInLiquid(0, -1, 0)) {
                return true;
            }
        
            // Some mobs spawn randomly over time, with a few additional spawn conditions unique to the entity
            // NOTE: A few mobs, like bats, put additional RNG and time restrictions in their spawn conditions.
            //       Some time fuzzing and averaging will be used to make the results more consistent.
            IBlockState blockState = world.getBlockState(pos);
            if (!blockState.isNormalCube()) {
                boolean canSpawnHere = false;
                MiscVanilla.enableTimeFuzz(world.isRemote);
                try {
                    canSpawnHere = ((EntityLiving)morphEntity).getCanSpawnHere();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MiscVanilla.disableTimeFuzz(world.isRemote);
                if (canSpawnHere) {
                    return true;
                }
            }
        }
        
        return false;
    }

    @Override
    public ITextComponent getFeelsAtHomeMessage(NeedStatus needStatus) {
        return new TextComponentTranslation("hardcorealchemy.instinct.home.generic.fulfilled");
    }

    @Override
    public ITextComponent getNotAtHomeMessage(NeedStatus needStatus) {
        return new TextComponentTranslation("hardcorealchemy.instinct.home.generic.need");
    }
    
    @Override
    public boolean doesReallyNotFeelAtHome() {
        return !feelsAtHome && averageAtHomeFrequency <= (1.0F / historyCapacity);
    }
    
    @Override
    public boolean doesReallyFeelAtHome() {
        return feelsAtHome && averageAtHomeFrequency > preferredAtHomeFrequency;
    }
    
    @Override
    public ITextComponent getNeedMessage(NeedStatus needStatus) {
        // Display a message only if it's clear if the player is "at home" or not
        if (doesReallyFeelAtHome()) {
            return getFeelsAtHomeMessage(needStatus);
        }
        else if (doesReallyNotFeelAtHome()) {
            return getNotAtHomeMessage(needStatus);
        }
        feelAtHomeMessageQueue = Math.max(feelAtHomeMessageQueue + 1, MAX_FEEL_AT_HOME_MESSAGE_QUEUE);
        return null;
    }
    
    @Override
    public void tick(IInstinctState instinctState) {
        EntityPlayer player = instinctState.getPlayer();
        feelsAtHome = doesPlayerFeelAtHome(player);
        if (feelsAtHome) {
            if (atHomeStreak < Integer.MAX_VALUE) {
                atHomeStreak++;
            }
        }
        else {
            atHomeStreak = 0;
        }
        maxAtHomeStreak = Math.max(maxAtHomeStreak, atHomeStreak);
        
        historyCapacity = Math.max(MIN_HISTORY_CAPACITY, MAX_HISTORY_CAPACITY - maxAtHomeStreak);
        averageAtHomeFrequency = ((averageAtHomeFrequency * (historyCapacity - 1)) + (feelsAtHome ? 1.0F : 0.0F)) / historyCapacity;
        if (atHomeStreak == 0) {
            // Prevent abnormal random output putting the player in a state where they can never feel "at home" anymore
            if (preferredAtHomeFrequency <= MAX_FREQUENCY_ALLOWING_DECAY) {
                preferredAtHomeFrequency -= PREFERRED_FREQUENCY_DECAY_RATE;
            }
        }
        else {
            preferredAtHomeFrequency = Math.max(averageAtHomeFrequency / 2.0F, preferredAtHomeFrequency);
        }
        
        boolean reallyFeelsAtHome = doesReallyFeelAtHome();
        boolean reallyFeelsNotAtHome = doesReallyNotFeelAtHome();
        
        if (reallyFeelsAtHome) {
            instinctState.setNeedStatus(IInstinctState.NeedStatus.NONE);
            if (shouldRandomlyDisplayFeelAtHomeMessage) {
                if (feelAtHomeMessageQueue > 0) {
                    // TODO: Message
                    feelAtHomeMessageQueue--;
                }
                shouldRandomlyDisplayFeelAtHomeMessage = false;
            }
        }
        else if (reallyFeelsNotAtHome) {
            instinctState.setNeedStatus(IInstinctState.NeedStatus.EVENTUALLY);
            shouldRandomlyDisplayFeelAtHomeMessage = true;
        }
    }

}