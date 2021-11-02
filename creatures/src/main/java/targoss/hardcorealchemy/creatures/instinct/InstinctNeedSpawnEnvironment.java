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

package targoss.hardcorealchemy.creatures.instinct;

import java.util.Random;

import javax.annotation.Nullable;

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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.creatures.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.creatures.instinct.api.IInstinctState;
import targoss.hardcorealchemy.creatures.instinct.api.IInstinctState.NeedStatus;
import targoss.hardcorealchemy.creatures.util.MorphSpawnEnvironment;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.MiscVanilla;
import targoss.hardcorealchemy.util.RandomUtil;
import targoss.hardcorealchemy.util.Serialization;

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
    
    protected Random random = new Random();
    
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
    protected static final int MAX_AT_HOME_MESSAGE_QUEUE = 3;
    protected int atHomeMessageQueue = 0;
    protected static final int MAX_SEES_HOME_MESSAGE_QUEUE = 1;
    protected int seesHomeMessageQueue = 0;
    protected boolean atHomeMessageEnabled = true;
    protected boolean seesHomeMessageEnabled = false;
    
    protected EntityLivingBase spawnCheckEntity = null;
    protected boolean usingProxyEntity = false;
    
    /**
     * For really volatile and random spawn checking, keep a long
     * history.
     */
    protected static final int VERY_SHORT_HOME_STREAK = 20;
    /**
     * With long streaks of successful spawn checks, a long history
     * prevents the player from receiving timely feedback. A higher
     * number here will cause the history to shrink faster when the streak
     * is no longer considered short.
     */
    protected static final int LONG_HOME_STREAK_HISTORY_LOSS = 40;
    
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
    
    protected static final int MAX_HOME_DESIRE_DISTANCE = 20;
    public BlockPos atHomeTestPos = null;
    public boolean atHomeTestPosNotQueued = false;
    
    public InstinctNeedSpawnEnvironment(EntityLivingBase morphEntity) {
        spawnCheckEntity = morphEntity;
        EntityLivingBase proxyEntity = MorphSpawnEnvironment.getSpawnCheckEntity(morphEntity);
        if (proxyEntity != null) {
            spawnCheckEntity = proxyEntity;
            usingProxyEntity = true;
        }
    }
    
    private static final String NBT_FEELS_AT_HOME = "feelsAtHome";
    private static final String NBT_AT_HOME_STREAK = "atHomeStreak";
    private static final String NBT_MAX_AT_HOME_STREAK = "maxAtHomeStreak";
    private static final String NBT_AVERAGE_AT_HOME_FRACTION = "averageAtHomeFraction";
    private static final String NBT_PREFERRED_AT_HOME_FRACTION = "preferredAtHomeFraction";
    private static final String NBT_AT_HOME_TEST_POS = "atHomeTestPos";

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        
        nbt.setBoolean(NBT_FEELS_AT_HOME, feelsAtHome);
        nbt.setInteger(NBT_AT_HOME_STREAK, atHomeStreak);
        nbt.setInteger(NBT_MAX_AT_HOME_STREAK, maxAtHomeStreak);
        nbt.setFloat(NBT_AVERAGE_AT_HOME_FRACTION, averageAtHomeFraction);
        nbt.setFloat(NBT_PREFERRED_AT_HOME_FRACTION, preferredAtHomeFraction);
        Serialization.setBlockPosNBT(nbt, NBT_AT_HOME_TEST_POS, atHomeTestPos);
        
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        feelsAtHome = nbt.getBoolean(NBT_FEELS_AT_HOME);
        atHomeStreak = nbt.getInteger(NBT_AT_HOME_STREAK);
        maxAtHomeStreak = nbt.getInteger(NBT_MAX_AT_HOME_STREAK);
        averageAtHomeFraction = nbt.getFloat(NBT_AVERAGE_AT_HOME_FRACTION);
        preferredAtHomeFraction = nbt.getFloat(NBT_PREFERRED_AT_HOME_FRACTION);
        atHomeTestPos = Serialization.getBlockPosNBT(nbt, NBT_AT_HOME_TEST_POS);
    }

    @Override
    public IInstinctNeed createInstanceFromMorphEntity(EntityLivingBase morphEntity) {
        return new InstinctNeedSpawnEnvironment(morphEntity);
    }
    
    // TODO: Cache biomes the entity is known to spawn in, and check during initialization if spawning of that entity was disabled in at least one of those biomes. If that happens, reset this need to avoid issues.

    public static boolean isGoodHomeLocation(World world, BlockPos pos, EntityLivingBase morphEntity) {
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
    
    public static boolean canPlayerFitAtPos(EntityPlayer player, BlockPos pos) {
        // Adapted from Entity.isEntityInsideOpaqueBlock()
        //TODO: Consider stronger criteria of the player being able to stand there in the first place, regardless of whether or not they would suffocate there
        
        BlockPos.PooledMutableBlockPos testPos = BlockPos.PooledMutableBlockPos.retain();

        for (int i = 0; i < 8; ++i)
        {
            int checkY = MathHelper.floor(pos.getY() + (double)(((float)((i >> 0) % 2) - 0.5F) * 0.1F) + (double)player.getEyeHeight());
            int checkX = MathHelper.floor(pos.getX() + (double)(((float)((i >> 1) % 2) - 0.5F) * player.width * 0.8F));
            int checkZ = MathHelper.floor(pos.getZ() + (double)(((float)((i >> 2) % 2) - 0.5F) * player.width * 0.8F));

            if (testPos.getX() != checkX || testPos.getY() != checkY || testPos.getZ() != checkZ)
            {
                testPos.setPos(checkX, checkY, checkZ);

                IBlockState blockState = player.world.getBlockState(testPos);
                if (blockState.getBlock().causesSuffocation())
                {
                    testPos.release();
                    return false;
                }
            }
        }

        testPos.release();
        return true;
    }
    
    @Override
    public boolean doesPlayerFeelAtHome(EntityPlayer player, @Nullable EntityLivingBase morphEntity) {
        if (morphEntity == null) {
            return true;
        }
        
        return isGoodHomeLocation(player.world, player.getPosition(), spawnCheckEntity);
    }
    
    protected void updatePlayerFeelsAtHome(EntityPlayer player, @Nullable EntityLivingBase morphEntity) {
        feelsAtHome = doesPlayerFeelAtHome(player, morphEntity);
        if (feelsAtHome) {
            atHomeTestPos = player.getPosition();
        }
    }
    
    /**
     * Updates atHomeTestPos to be non-null if the new (possibly unchanged) test position is a valid place for the player to spawn.
     * The test position must be in line of sight at first, but otherwise continues to entice the player
     * until it is no longer a valid home position, or is too far away.
     * */
    protected void updateAtHomeTestPos(EntityPlayer player, @Nullable EntityLivingBase morphEntity) {
        if (morphEntity == null) {
            atHomeTestPos = null;
            return;
        }
        if (feelsAtHome) {
            // Already non-null after updatePlayerFeelsAtHome called
            return;
        }
        
        boolean atHomeTestPosWasNull = atHomeTestPos == null;
        if (!atHomeTestPosWasNull) {
            BlockPos playerPos = player.getPosition();
            int dx = playerPos.getX() - atHomeTestPos.getX();
            int dy = playerPos.getY() - atHomeTestPos.getY();
            int dz = playerPos.getZ() - atHomeTestPos.getZ();
            int distanceSquared = (dx*dx) + (dy*dy) + (dz*dz);
            if (distanceSquared > MAX_HOME_DESIRE_DISTANCE*MAX_HOME_DESIRE_DISTANCE ||
                    !isGoodHomeLocation(player.world, atHomeTestPos, spawnCheckEntity) ||
                    !canPlayerFitAtPos(player, atHomeTestPos)) {
                atHomeTestPos = null;
            } else {
                return;
            }
        }
        
        BlockPos testPos;
        if (atHomeTestPos == null) {
            // Need new candidate test position
            Vec3d playerPosD = new Vec3d(player.getPosition()).addVector(0.0D, player.height * 0.5D, 0.0D);
            Vec3d traceDirection = RandomUtil.getRandomDirection(random).scale(MAX_HOME_DESIRE_DISTANCE);
            Vec3d lastTracePos = playerPosD.add(traceDirection);
            RayTraceResult res = player.world.rayTraceBlocks(playerPosD, lastTracePos, false, true, false);
            if (res == null) {
                // Could not find candidate test position this time
                atHomeTestPosNotQueued |= !atHomeTestPosWasNull;
                return;
            }
            testPos = res.getBlockPos();
            if (testPos == null) {
                // Could not find candidate test position this time
                atHomeTestPosNotQueued |= !atHomeTestPosWasNull;
                return;
            }
            // Wherever the block hits, check one block higher for the actual spawn test location
            testPos = testPos.up();
            
        } else {
            testPos = atHomeTestPos;
        }
        
        // testPos is valid and the player fits there, but is it a good place for the player to be?
        if (isGoodHomeLocation(player.world, testPos, morphEntity) &&
               canPlayerFitAtPos(player, testPos)) {
            atHomeTestPos = testPos;
            atHomeTestPosNotQueued = true;
        } else {
            atHomeTestPos = null;
            atHomeTestPosNotQueued |= !atHomeTestPosWasNull;
        }
    }

    @Override
    public ITextComponent getFeelsAtHomeMessage(NeedStatus needStatus) {
        return new TextComponentTranslation("hardcorealchemy.instinct.home.generic.fulfilled");
    }
    
    public ITextComponent getNearHomeMessage() {
        return new TextComponentTranslation("hardcorealchemy.instinct.home.generic.need_nearby");
    }
    
    public ITextComponent getFarFromHomeMessage() {
        return new TextComponentTranslation("hardcorealchemy.instinct.home.generic.need");
    }

    @Override
    public ITextComponent getNotAtHomeMessage(NeedStatus needStatus) {
        if (atHomeTestPos == null) {
            return getFarFromHomeMessage();
        } else {
            return getNearHomeMessage();
        }
    }
    
    @Override
    public boolean doesReallyNotFeelAtHome() {
        return !feelsAtHome && preferredAtHomeFraction != 0.0F && averageAtHomeFraction <= (preferredAtHomeFraction * 0.5);
    }
    
    @Override
    public boolean doesReallyFeelAtHome() {
        return feelsAtHome && averageAtHomeFraction > preferredAtHomeFraction;
    }
    
    @Override
    public ITextComponent getNeedMessage(NeedStatus needStatus) {
        // Display a message only if it's clear if the player is "at home" or not
        if (doesReallyFeelAtHome()) {
            seesHomeMessageQueue = Math.min(seesHomeMessageQueue + 1, MAX_SEES_HOME_MESSAGE_QUEUE);
            return getFeelsAtHomeMessage(needStatus);
        }
        else if (doesReallyNotFeelAtHome()) {
            return getNotAtHomeMessage(needStatus);
        }
        atHomeMessageQueue = Math.min(atHomeMessageQueue + 1, MAX_AT_HOME_MESSAGE_QUEUE);
        seesHomeMessageQueue = Math.min(seesHomeMessageQueue + 1, MAX_SEES_HOME_MESSAGE_QUEUE);
        return null;
    }

    @Override
    public ITextComponent getNeedUnfulfilledMessage(NeedStatus needStatus) {
        return null;
    }
    
    @Override
    public void tick(IInstinctState instinctState) {
        EntityPlayer player = instinctState.getPlayer();
        if (player.world.isRemote) {
            return;
        }
        
        EntityLivingBase morphEntity = null;
        IMorphing morphing = player.getCapability(MORPHING_CAPABILITY, null);
        if (morphing != null) {
            AbstractMorph morph = morphing.getCurrentMorph();
            if (morph != null && (morph instanceof EntityMorph)) {
                morphEntity = ((EntityMorph)morph).getEntity(player.world);
            }
        }
        
        if (usingProxyEntity) {
            spawnCheckEntity.world = player.world;
            spawnCheckEntity.setPosition(player.posX, player.posY, player.posZ);
            
        }
        
        updatePlayerFeelsAtHome(player, morphEntity);
        updateAtHomeTestPos(player, morphEntity);
        if (feelsAtHome) {
            if (atHomeStreak < Integer.MAX_VALUE) {
                atHomeStreak++;
            }
        }
        else {
            atHomeStreak = 0;
        }
        maxAtHomeStreak = Math.max(maxAtHomeStreak, atHomeStreak);

        int historyCapacityLoss = maxAtHomeStreak < VERY_SHORT_HOME_STREAK ? maxAtHomeStreak : (LONG_HOME_STREAK_HISTORY_LOSS * maxAtHomeStreak);
        historyCapacity = Math.max(MIN_HISTORY_CAPACITY, MAX_HISTORY_CAPACITY - historyCapacityLoss);
        float atHomeGain = 1.0F / (float)(historyCapacity);
        float atHomeDecay = (float)(historyCapacity - 1) * atHomeGain;
        averageAtHomeFraction = (averageAtHomeFraction * atHomeDecay) + (feelsAtHome ? atHomeGain : 0.0F);
        if (atHomeStreak == 0) {
            // Prevent abnormal random output putting the player in a state where they can never feel "at home" anymore
            if (maxAtHomeStreak <= MAX_FREQUENCY_ALLOWING_DECAY) {
                preferredAtHomeFraction -= PREFERRED_FREQUENCY_DECAY_RATE;
                if (preferredAtHomeFraction < 0.0F) {
                    preferredAtHomeFraction = 0.0F;
                }
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
                        ITextComponent feelsAtHomeMessage = getNeedMessage(NeedStatus.NONE);
                        Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, feelsAtHomeMessage);
                    }
                    atHomeMessageQueue--;
                }
                atHomeMessageEnabled = false;
            }
            seesHomeMessageEnabled = true;
        }
        else if (reallyFeelsNotAtHome) {
            if (atHomeTestPos == null) {
                instinctState.setNeedStatus(IInstinctState.NeedStatus.EVENTUALLY);
            } else {
                instinctState.setNeedStatus(IInstinctState.NeedStatus.URGENT);
                if (seesHomeMessageQueue > 0) {
                    if (!player.world.isRemote) {
                        ITextComponent feelsAtHomeMessage = getNeedMessage(NeedStatus.URGENT);
                        Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, feelsAtHomeMessage);
                    }
                    seesHomeMessageQueue--;
                }
                seesHomeMessageEnabled = false;
            }
            atHomeMessageEnabled = true;
            // Experimental. Still trying to figure out when to display the message without it feeling "spammy".
            atHomeMessageQueue = Math.max(1, atHomeMessageQueue);
        }
    }
}
