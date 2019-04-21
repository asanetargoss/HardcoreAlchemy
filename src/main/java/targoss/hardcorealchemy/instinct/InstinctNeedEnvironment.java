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

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import targoss.hardcorealchemy.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.instinct.api.IInstinctState;
import targoss.hardcorealchemy.instinct.api.IInstinctState.NeedStatus;
import targoss.hardcorealchemy.instinct.api.InstinctNeedFactory;
import targoss.hardcorealchemy.util.MobLists;

/**
 * An instinct need to be in certain environmental conditions
 */
public abstract class InstinctNeedEnvironment implements IInstinctNeed {
    public static class Factory extends InstinctNeedFactory {
        private static List<Class<? extends EntityLivingBase>> grassMobs = new ArrayList<>();
        static {
            for (String grassMobName : MobLists.getGrassMobs()) {
                if (!EntityList.isStringValidEntityName(grassMobName)) {
                    continue;
                }
                grassMobs.add((Class<? extends EntityLivingBase>)EntityList.getClassFromID(EntityList.getIDFromString(grassMobName)));
            }
        }
        
        @Override
        public IInstinctNeed createNeed(EntityLivingBase morphEntity) {
            // TODO: Other environments
            // TODO: Support mobs which like multiple environments
            for (Class<? extends EntityLivingBase> grassMobType : grassMobs) {
                if (grassMobType.isInstance(morphEntity)) {
                    return new InstinctNeedForestPlains(morphEntity);
                }
            }
            return null;
        }
    }
    
    public InstinctNeedEnvironment(EntityLivingBase entityMorphedAs) {}
    
    /** Establishes a hysteresis range for checking if a player's environment is good or bad.
     * Default 10 seconds. */
    protected int homeTimeBuffer = 10 * 20;
    
    /** Higher means the player has felt at home for longer.
     * Serves as a buffer against player constantly moving between good/bad environments. */
    protected int atHomeWarmup = 0;
    /** Total time spent not at home, used to determine amplitude of some effects */
    protected int timeNotAtHome = 0;

    public static final String NBT_AT_HOME_WARMUP = "atHomeWarmup";
    public static final String NBT_TIME_NOT_AT_HOME = "timeNotAtHome";

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        
        nbt.setInteger(NBT_AT_HOME_WARMUP, atHomeWarmup);
        nbt.setInteger(NBT_TIME_NOT_AT_HOME, timeNotAtHome);
        
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        atHomeWarmup = nbt.getInteger(NBT_AT_HOME_WARMUP);
        timeNotAtHome = nbt.getInteger(NBT_TIME_NOT_AT_HOME);
    }

    /**
     * Calculates how well-suited the player is to their current environment.
     * Potentially called every tick.
     * */
    public abstract boolean doesPlayerFeelAtHome(EntityPlayer player);
    
    public boolean doesReallyFeelAtHome() {
        return atHomeWarmup > homeTimeBuffer / 2;
    }

    public boolean doesReallyNotFeelAtHome() {
        return atHomeWarmup == 0;
    }

    public abstract ITextComponent getFeelsAtHomeMessage(NeedStatus needStatus);
    
    public abstract ITextComponent getNotAtHomeMessage(NeedStatus needStatus);

    @Override
    public ITextComponent getNeedMessage(NeedStatus needStatus) {
        // Display a message only if it's clear if the player is "at home" or not
        if (doesReallyFeelAtHome()) {
            return getFeelsAtHomeMessage(needStatus);
        }
        else if (doesReallyNotFeelAtHome()) {
            return getNotAtHomeMessage(needStatus);
        }
        return null;
    }

    @Override
    public ITextComponent getNeedUnfulfilledMessage(NeedStatus needStatus) {
        return null;
    }
    
    public void tickFeelingAtHome(EntityPlayer player) {
        // Use a counter to create a hysteresis range so status effects aren't constantly enabled/disabled
        if (doesPlayerFeelAtHome(player)) {
            atHomeWarmup = Math.min(atHomeWarmup + 1, homeTimeBuffer);
        }
        else {
            atHomeWarmup = Math.max(atHomeWarmup - 1, 0);
        }
    }
    
    @Override
    public void tick(IInstinctState instinctState) {
        // Calculate instantaneous environment happiness based on where the player is located
        tickFeelingAtHome(instinctState.getPlayer());
        
        // Do the actual need checks and apply them to the instinctState
        if (doesReallyFeelAtHome()) {
            instinctState.setNeedStatus(IInstinctState.NeedStatus.NONE);
        }
        else if (doesReallyNotFeelAtHome()) {
            instinctState.setNeedStatus(IInstinctState.NeedStatus.EVENTUALLY);
        }
    }

}
