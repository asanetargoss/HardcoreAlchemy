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

package targoss.hardcorealchemy.capability.inactive;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import targoss.hardcorealchemy.capability.inactive.IInactiveCapabilities.Cap;

public class StorageInactiveCapabilities implements Capability.IStorage<IInactiveCapabilities> {
    private static final String SHOULD_CLONE = "shouldClone";
    private static final String DATA = "data";
    
    @Override
    public NBTBase writeNBT(Capability<IInactiveCapabilities> capability, IInactiveCapabilities instance,
            EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        
        for (Map.Entry<String, Cap> capInfo : instance.getCapabilityMap().entrySet()) {
            Cap cap = capInfo.getValue();
            NBTTagCompound data = cap.data;
            if (data == null) {
                continue;
            }
            
            NBTTagCompound capNbt = new NBTTagCompound();
            capNbt.setBoolean(SHOULD_CLONE, cap.persistsOnDeath);
            capNbt.setTag(DATA, cap.data.copy());
            
            nbt.setTag(capInfo.getKey(), capNbt);
        }
        
        return nbt;
    }

    @Override
    public void readNBT(Capability<IInactiveCapabilities> capability, IInactiveCapabilities instance, EnumFacing side,
            NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        
        ConcurrentMap capabilityMap = new ConcurrentHashMap<String, Cap>();
        
        for (String key : nbtCompound.getKeySet()) {
            NBTBase nbtEntry = nbtCompound.getTag(key);
            if (!(nbtEntry instanceof NBTTagCompound)) {
                continue;
            }
            NBTTagCompound compoundEntry = (NBTTagCompound)nbtEntry;
            
            NBTBase nbtData = compoundEntry.getTag(DATA);
            NBTTagCompound compoundData = (nbtData instanceof NBTTagCompound) ? (NBTTagCompound)nbtData : new NBTTagCompound();
            
            Cap cap = new Cap();
            cap.persistsOnDeath = compoundEntry.getBoolean(SHOULD_CLONE);
            cap.data = compoundData.copy();
            
            capabilityMap.put(key, cap);
        }
        
        instance.setCapabilityMap(capabilityMap);
    }
    
}
