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

package targoss.hardcorealchemy.capability.worldhumanity;

import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import targoss.hardcorealchemy.capability.worldhumanity.ICapabilityWorldHumanity.MorphAbilityLocation;
import targoss.hardcorealchemy.util.Serialization;

public class StorageWorldHumanity implements Capability.IStorage<ICapabilityWorldHumanity> {
    protected static final String MORPH_ABILITY_LOCATIONS = "morph_ability_locations";
    protected static final String MORPH_ABILITY_LOCATION_LIFETIME_UUID = "lifetime_uuid";
    protected static final String MORPH_ABILITY_LOCATION_PLAYER_UUID = "player_uuid";
    protected static final String MORPH_ABILITY_LOCATION_POS = "pos";

    @Override
    public NBTBase writeNBT(Capability<ICapabilityWorldHumanity> capability, ICapabilityWorldHumanity instance,
            EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        
        {
            NBTTagList nbtMorphAbilityLocations = new NBTTagList();
            MorphAbilityLocation[] morphAbilityLocations = instance.dumpMorphAbilityLocations();
            for (MorphAbilityLocation morphAbilityLocation : morphAbilityLocations) {
                NBTTagCompound nbtMorphAbilityLocation = new NBTTagCompound();
                nbtMorphAbilityLocation.setTag(MORPH_ABILITY_LOCATION_LIFETIME_UUID, NBTUtil.createUUIDTag(morphAbilityLocation.lifetimeUUID));
                nbtMorphAbilityLocation.setTag(MORPH_ABILITY_LOCATION_PLAYER_UUID, NBTUtil.createUUIDTag(morphAbilityLocation.playerUUID));
                nbtMorphAbilityLocation.setTag(MORPH_ABILITY_LOCATION_POS, NBTUtil.createPosTag(morphAbilityLocation.pos));
                nbtMorphAbilityLocations.appendTag(nbtMorphAbilityLocation);
            }
            nbt.setTag(MORPH_ABILITY_LOCATIONS, nbtMorphAbilityLocations);
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
        
        if (nbtCompound.hasKey(MORPH_ABILITY_LOCATIONS, Serialization.NBT_LIST_ID)) {
            NBTTagList nbtMorphAbilityLocations = (NBTTagList)nbtCompound.getTag(MORPH_ABILITY_LOCATIONS);
            final int n = nbtMorphAbilityLocations.tagCount();
            
            ArrayList<MorphAbilityLocation> morphAbilityLocations = new ArrayList<>(n);
            for (int i = 0; i < n; ++i) {
                NBTTagCompound nbtMorphAbilityLocation = nbtMorphAbilityLocations.getCompoundTagAt(i);
                if (!nbtMorphAbilityLocation.hasKey(MORPH_ABILITY_LOCATION_LIFETIME_UUID, Serialization.NBT_COMPOUND_ID)) {
                    continue;
                }
                UUID lifetimeUUID = NBTUtil.getUUIDFromTag(nbtMorphAbilityLocation.getCompoundTag(MORPH_ABILITY_LOCATION_LIFETIME_UUID));
                if (!nbtMorphAbilityLocation.hasKey(MORPH_ABILITY_LOCATION_PLAYER_UUID, Serialization.NBT_COMPOUND_ID)) {
                    continue;
                }
                UUID playerUUID = NBTUtil.getUUIDFromTag(nbtMorphAbilityLocation.getCompoundTag(MORPH_ABILITY_LOCATION_PLAYER_UUID));
                if (!nbtMorphAbilityLocation.hasKey(MORPH_ABILITY_LOCATION_POS, Serialization.NBT_COMPOUND_ID)) {
                    continue;
                }
                BlockPos pos = NBTUtil.getPosFromTag(nbtMorphAbilityLocation.getCompoundTag(MORPH_ABILITY_LOCATION_POS));
                
                MorphAbilityLocation morphAbilityLocation = new MorphAbilityLocation(lifetimeUUID, playerUUID, pos);
                morphAbilityLocations.add(morphAbilityLocation);
            }
            
            instance.clearAndPutMorphAbilityLocations((MorphAbilityLocation[])morphAbilityLocations.toArray());
        }
    }
}
