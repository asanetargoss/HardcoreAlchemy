/*
 * Copyright 2017-2023 asanetargoss
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

package targoss.hardcorealchemy.creatures.capability.killcount;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import targoss.hardcorealchemy.util.Serialization;

public class StorageKillCount implements Capability.IStorage<ICapabilityKillCount> {
    protected static final String KILL_COUNTS = "kill_counts";
    protected static final String MASTERED_KILLS = "mastered_kills";

    @Override
    public NBTBase writeNBT(Capability<ICapabilityKillCount> capability, ICapabilityKillCount instance, EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        
        NBTTagCompound nbtKills = new NBTTagCompound();
        Map<String, Integer> killCounts = instance.getKillCounts();
        for (Entry<String, Integer> killCount : killCounts.entrySet()) {
            String stringKey = killCount.getKey();
            if (stringKey == null || stringKey.equals("")) {
                continue;
            }
            nbtKills.setInteger(stringKey, killCount.getValue());
        }
        nbt.setTag(KILL_COUNTS, nbtKills);
        

        NBTTagList nbtMasteredKills = new NBTTagList();
        Set<String> masteredKills = instance.getMasteredKills();
        for (String kill : masteredKills) {
            if (kill == null || kill.equals("")) {
                continue;
            }
            nbtMasteredKills.appendTag(new NBTTagString(kill));
        }
        nbt.setTag(MASTERED_KILLS, nbtMasteredKills);
        
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityKillCount> capability, ICapabilityKillCount instance, EnumFacing side, NBTBase nbt) {
        if (nbt.hasNoTags() || !(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;

        Map<String, Integer> killCounts = new HashMap<String, Integer>();
        if (nbtCompound.hasKey(KILL_COUNTS, Serialization.NBT_COMPOUND_ID)) {
            NBTTagCompound nbtKills = nbtCompound.getCompoundTag("kill_counts");
            for (String key : nbtKills.getKeySet()) {
                if (key.equals("")) {
                    continue;
                }
                if (EntityList.NAME_TO_CLASS.containsKey(key)) {
                    killCounts.put(key, nbtKills.getInteger(key));
                }
            }
        }
        instance.setKillCounts(killCounts);
        
        Set<String> masteredKills = new HashSet<String>();
        if (nbtCompound.hasKey(MASTERED_KILLS, Serialization.NBT_LIST_ID)) {
            NBTTagList nbtMasteredKills = nbtCompound.getTagList(MASTERED_KILLS, Serialization.NBT_STRING_ID);
            for (int i = 0; i < nbtMasteredKills.tagCount(); ++i) {
                String kill = nbtMasteredKills.getStringTagAt(i);
                if (kill.equals("")) {
                    continue;
                }
                if (EntityList.NAME_TO_CLASS.containsKey(kill)) {
                    masteredKills.add(kill);
                }
            }
        }
        instance.setMasteredKills(masteredKills);
    }

}
