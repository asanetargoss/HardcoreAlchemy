/*
 * Copyright 2017-2018 asanetargoss
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

package targoss.hardcorealchemy.capability.killcount;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class StorageKillCount implements Capability.IStorage<ICapabilityKillCount> {

    @Override
    public NBTBase writeNBT(Capability<ICapabilityKillCount> capability, ICapabilityKillCount instance, EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagCompound nbtKills = new NBTTagCompound();
        Map<String, Integer> killCounts = instance.getKillCounts();
        for (Entry<String, Integer> killCount : killCounts.entrySet()) {
            String stringKey = killCount.getKey();
            if (stringKey.equals("")) {
                // Empty string not allowed!
                continue;
            }
            nbtKills.setInteger(stringKey, killCount.getValue());
        }
        nbt.setTag("kill_counts", nbtKills);
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityKillCount> capability, ICapabilityKillCount instance, EnumFacing side, NBTBase nbt) {
        Map<String, Integer> killCounts = new HashMap<String, Integer>();
        if (nbt.hasNoTags() || !(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        NBTTagCompound nbtKills = nbtCompound.getCompoundTag("kill_counts");
        for (String key : nbtKills.getKeySet()) {
            if (key.equals("")) {
                // Empty string not allowed!
                continue;
            }
            if (EntityList.NAME_TO_CLASS.containsKey(key)) {
                killCounts.put(key, nbtKills.getInteger(key));
            }
        }
        instance.setKillCounts(killCounts);
    }

}
