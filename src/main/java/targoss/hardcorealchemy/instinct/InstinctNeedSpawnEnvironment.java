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

import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.EntityMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.instinct.api.IInstinctState;
import targoss.hardcorealchemy.instinct.api.IInstinctState.NeedStatus;
import targoss.hardcorealchemy.instinct.network.NeedMessengerSpawnEnvironment;
import targoss.hardcorealchemy.instinct.network.api.INeedMessenger;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.MiscVanilla;
import targoss.hardcorealchemy.util.MorphSpawnEnvironment;

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
public class InstinctNeedSpawnEnvironment implements IInstinctNeedEnvironment {
    
    @CapabilityInject(IMorphing.class)
    private static final Capability<IMorphing> MORPHING_CAPABILITY = null;
    
    protected static final int MIN_HISTORY_CAPACITY = 10 * 20;
    protected static final int MAX_HISTORY_CAPACITY = 4096;
    /**
     * Math.max(MIN_HISTORY_CAPACITY, MAX_HISTORY_CAPACITY - maxAtHomeStreak)
     */
    protected int historyCapacity = MAX_HISTORY_CAPACITY;
    /**
     * In case some entities have extremely rare spawn success,
     * allow preferredAtHomeFraction to decay if maxAtHomeStreak
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
    protected int atHomeMessageQueue = 0;
    protected boolean atHomeMessageEnabled = true;
    
    protected EntityLivingBase spawnCheckEntity = null;
    protected boolean usingProxyEntity = false;
    
    public boolean feelsAtHome = false;
    public int atHomeStreak = 0;
    public int maxAtHomeStreak = 0;
    public float averageAtHomeFraction = 0.0F;
    /**
     * The preferred number of ticks (relative to the environment history size) at which the
     * player starts to feel at home. When above this value, the player will not feel negative effects.
     * The value of this will be no greater than the maximum recorded averageAtHomeFraction / 2
     */
    public float preferredAtHomeFraction = 0.0F;
    
    public InstinctNeedSpawnEnvironment(EntityLivingBase morphEntity) {
        spawnCheckEntity = morphEntity;
        EntityLivingBase proxyEntity = MorphSpawnEnvironment.getSpawnCheckEntity(morphEntity);
        if (proxyEntity != null) {
            spawnCheckEntity = proxyEntity;
            usingProxyEntity = true;
        }
    }
    
    @Override
    public INeedMessenger getCustomMessenger() {
        return new NeedMessengerSpawnEnvironment();
    }
    
    private static final String NBT_FEELS_AT_HOME = "feelsAtHome";
    private static final String NBT_AT_HOME_STREAK = "atHomeStreak";
    private static final String NBT_MAX_AT_HOME_STREAK = "maxAtHomeStreak";
    private static final String NBT_AVERAGE_AT_HOME_FRACTION = "averageAtHomeFraction";
    private static final String NBT_PREFERRED_AT_HOME_FRACTION = "preferredAtHomeFraction";

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        
        nbt.setBoolean(NBT_FEELS_AT_HOME, feelsAtHome);
        nbt.setInteger(NBT_AT_HOME_STREAK, atHomeStreak);
        nbt.setInteger(NBT_MAX_AT_HOME_STREAK, maxAtHomeStreak);
        nbt.setFloat(NBT_AVERAGE_AT_HOME_FRACTION, averageAtHomeFraction);
        nbt.setFloat(NBT_PREFERRED_AT_HOME_FRACTION, preferredAtHomeFraction);
        
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        feelsAtHome = nbt.getBoolean(NBT_FEELS_AT_HOME);
        atHomeStreak = nbt.getInteger(NBT_AT_HOME_STREAK);
        maxAtHomeStreak = nbt.getInteger(NBT_MAX_AT_HOME_STREAK);
        averageAtHomeFraction = nbt.getFloat(NBT_AVERAGE_AT_HOME_FRACTION);
        preferredAtHomeFraction = nbt.getFloat(NBT_PREFERRED_AT_HOME_FRACTION);
    }

    @Override
    public IInstinctNeed createInstanceFromMorphEntity(EntityLivingBase morphEntity) {
        return new InstinctNeedSpawnEnvironment(morphEntity);
    }
    
    // TODO: Cache biomes the entity is known to spawn in, and check during initialization if spawning of that entity was disabled in at least one of those biomes. If that happens, reset this need to avoid issues.

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
        return !feelsAtHome && averageAtHomeFraction <= (1.0F / historyCapacity);
    }
    
    @Override
    public boolean doesReallyFeelAtHome() {
        return feelsAtHome && averageAtHomeFraction > preferredAtHomeFraction;
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
        atHomeMessageQueue = Math.max(atHomeMessageQueue + 1, MAX_FEEL_AT_HOME_MESSAGE_QUEUE);
        return null;
    }

    @Override
    public ITextComponent getNeedUnfulfilledMessage(NeedStatus needStatus) {
        return null;
    }
    
    @Override
    public void tick(IInstinctState instinctState) {
        EntityPlayer player = instinctState.getPlayer();
        
        if (usingProxyEntity) {
            spawnCheckEntity.world = player.world;
            spawnCheckEntity.setPosition(player.posX, player.posY, player.posZ);
            
        }
        
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
        averageAtHomeFraction = ((averageAtHomeFraction * (historyCapacity - 1)) + (feelsAtHome ? 1.0F : 0.0F)) / historyCapacity;
        if (atHomeStreak == 0) {
            // Prevent abnormal random output putting the player in a state where they can never feel "at home" anymore
            if (preferredAtHomeFraction <= MAX_FREQUENCY_ALLOWING_DECAY) {
                preferredAtHomeFraction -= PREFERRED_FREQUENCY_DECAY_RATE;
            }
        }
        else {
            preferredAtHomeFraction = Math.max(averageAtHomeFraction / 2.0F, preferredAtHomeFraction);
        }
        
        boolean reallyFeelsAtHome = doesReallyFeelAtHome();
        boolean reallyFeelsNotAtHome = doesReallyNotFeelAtHome();
        
        if (reallyFeelsAtHome) {
            instinctState.setNeedStatus(IInstinctState.NeedStatus.NONE);
            if (atHomeMessageEnabled) {
                if (atHomeMessageQueue > 0) {
                    if (!player.world.isRemote) {
                        ITextComponent feelsAtHomeMessage = getFeelsAtHomeMessage(NeedStatus.NONE);
                        Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, feelsAtHomeMessage);
                    }
                    
                    atHomeMessageQueue--;
                }
                atHomeMessageEnabled = false;
            }
        }
        else if (reallyFeelsNotAtHome) {
            instinctState.setNeedStatus(IInstinctState.NeedStatus.EVENTUALLY);
            atHomeMessageEnabled = true;
        }
        
        if (!player.world.isRemote) {
            // Check if we need to sync data
            ((NeedMessengerSpawnEnvironment)instinctState.getNeedMessenger()).serverTick(this);
        }
    }
}
