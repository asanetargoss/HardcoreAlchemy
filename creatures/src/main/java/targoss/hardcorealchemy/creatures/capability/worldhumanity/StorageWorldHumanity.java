/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures.capability.worldhumanity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import targoss.hardcorealchemy.creatures.capability.worldhumanity.ICapabilityWorldHumanity.Phylactery;
import targoss.hardcorealchemy.util.Serialization;

public class StorageWorldHumanity implements Capability.IStorage<ICapabilityWorldHumanity> {
    protected static final String PHYLACTERIES = "morph_abilities";
    protected static final String PHYLACTERY_LIFETIME_UUID = "lifetime_uuid";
    protected static final String PHYLACTERY_PLAYER_UUID = "player_uuid";
    protected static final String PHYLACTERY_POS = "pos";
    protected static final String PHYLACTERY_DIMENSION = "dimension";
    protected static final String PHYLACTERY_STATE = "state";
    protected static final String PHYLACTERY_MORPH_TARGET = "morph_target";
    
    protected static byte stateFromEnum(ICapabilityWorldHumanity.State en) {
        switch (en) {
        case ACTIVE:
            return 0;
        case DORMANT:
            return 1;
        case DEACTIVATED:
            return 2;
        case REINCARNATED:
            return 3;
        default:
            return -1;
        }
    }
    
    protected static ICapabilityWorldHumanity.State enumFromState(byte st) {
        switch (st) {
        case 0:
            return ICapabilityWorldHumanity.State.ACTIVE;
        case 1:
            return ICapabilityWorldHumanity.State.DORMANT;
        case 2:
            return ICapabilityWorldHumanity.State.DEACTIVATED;
        case 3:
            return ICapabilityWorldHumanity.State.REINCARNATED;
        default:
            return ICapabilityWorldHumanity.State.DEACTIVATED;
        }
    }

    @Override
    public NBTBase writeNBT(Capability<ICapabilityWorldHumanity> capability, ICapabilityWorldHumanity instance,
            EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        
        {
            NBTTagList nbtPhylacteries = new NBTTagList();
            Collection<Phylactery> phylacteries = instance.dumpPhylacteries();
            for (Phylactery phylactery : phylacteries) {
                NBTTagCompound nbtPhylactery = new NBTTagCompound();
                nbtPhylactery.setTag(PHYLACTERY_LIFETIME_UUID, NBTUtil.createUUIDTag(phylactery.lifetimeUUID));
                nbtPhylactery.setTag(PHYLACTERY_PLAYER_UUID, NBTUtil.createUUIDTag(phylactery.playerUUID));
                nbtPhylactery.setTag(PHYLACTERY_POS, NBTUtil.createPosTag(phylactery.pos));
                nbtPhylactery.setInteger(PHYLACTERY_DIMENSION, phylactery.dimension);
                nbtPhylactery.setByte(PHYLACTERY_STATE, stateFromEnum(phylactery.state));      
                nbtPhylactery.setTag(PHYLACTERY_MORPH_TARGET, phylactery.morphTarget.toNBT());
                nbtPhylacteries.appendTag(nbtPhylactery);
            }
            nbt.setTag(PHYLACTERIES, nbtPhylacteries);
        }
        
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityWorldHumanity> capability, ICapabilityWorldHumanity instance,
            EnumFacing side, NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        
        if (nbtCompound.hasKey(PHYLACTERIES, Serialization.NBT_LIST_ID)) {
            NBTTagList nbtPhylacteries = (NBTTagList)nbtCompound.getTag(PHYLACTERIES);
            final int n = nbtPhylacteries.tagCount();
            
            ArrayList<Phylactery> phylacteries = new ArrayList<>(n);
            for (int i = 0; i < n; ++i) {
                NBTTagCompound nbtPhylactery = nbtPhylacteries.getCompoundTagAt(i);
                if (!nbtPhylactery.hasKey(PHYLACTERY_LIFETIME_UUID, Serialization.NBT_COMPOUND_ID)) {
                    continue;
                }
                UUID lifetimeUUID = NBTUtil.getUUIDFromTag(nbtPhylactery.getCompoundTag(PHYLACTERY_LIFETIME_UUID));
                if (!nbtPhylactery.hasKey(PHYLACTERY_PLAYER_UUID, Serialization.NBT_COMPOUND_ID)) {
                    continue;
                }
                UUID playerUUID = NBTUtil.getUUIDFromTag(nbtPhylactery.getCompoundTag(PHYLACTERY_PLAYER_UUID));
                if (!nbtPhylactery.hasKey(PHYLACTERY_POS, Serialization.NBT_COMPOUND_ID)) {
                    continue;
                }
                BlockPos pos = NBTUtil.getPosFromTag(nbtPhylactery.getCompoundTag(PHYLACTERY_POS));
                int dimension = nbtPhylactery.getInteger(PHYLACTERY_DIMENSION);
                byte stateByte = nbtPhylactery.getByte(PHYLACTERY_STATE);
                ICapabilityWorldHumanity.State state = enumFromState(stateByte);
                if (!nbtPhylactery.hasKey(PHYLACTERY_MORPH_TARGET, Serialization.NBT_COMPOUND_ID)) {
                    continue;
                }
                AbstractMorph morphTarget = MorphManager.INSTANCE.morphFromNBT(nbtPhylactery.getCompoundTag(PHYLACTERY_MORPH_TARGET));
                
                Phylactery phylactery = new Phylactery(lifetimeUUID, playerUUID, pos, dimension, state, morphTarget);
                phylacteries.add(phylactery);
            }
            
            instance.clearAndPutPhylacteries(phylacteries);
        }
    }
}
