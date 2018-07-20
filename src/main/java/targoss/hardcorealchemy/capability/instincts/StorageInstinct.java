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

package targoss.hardcorealchemy.capability.instincts;

import java.util.Map;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import targoss.hardcorealchemy.capability.instincts.ICapabilityInstinct.InstinctEntry;
import targoss.hardcorealchemy.instinct.IInstinct;
import targoss.hardcorealchemy.instinct.Instincts;

public class StorageInstinct implements Capability.IStorage<ICapabilityInstinct> {
    public static final String INSTINCT = "instinct";
    public static final String INACTIVE_INSTINCT_TIME = "inactiveInstinctTime";
    public static final String ACTIVE_INSTINCT = "active_instinct";
    
    public static final String INSTINCTS = "instincts";
    public static final String INSTINCT_ID = "id";
    public static final String INSTINCT_DATA = "data";
    public static final String INSTINCT_WEIGHT = "weight";
    
    @Override
    public NBTBase writeNBT(Capability<ICapabilityInstinct> capability, ICapabilityInstinct instance, EnumFacing side) {
        NBTTagCompound nbtCompound = new NBTTagCompound();
        
        nbtCompound.setFloat(INSTINCT, instance.getInstinct());
        nbtCompound.setFloat(INACTIVE_INSTINCT_TIME, instance.getInactiveInstinctTime());
        
        ResourceLocation activeInstinct = instance.getActiveInstinctId();
        if (activeInstinct != null) {
            nbtCompound.setString(ACTIVE_INSTINCT, activeInstinct.toString());
        } else {
            nbtCompound.setString(ACTIVE_INSTINCT, "");
        }
        
        NBTTagList instinctsNbt = new NBTTagList();
        for (InstinctEntry entry : instance.getInstinctMap().values()) {
            NBTTagCompound entryNbt = new NBTTagCompound();
            entryNbt.setString(INSTINCT_ID, entry.id.toString());
            entryNbt.setTag(INSTINCT_DATA, entry.instinct.serializeNBT());
            entryNbt.setFloat(INSTINCT_WEIGHT, entry.weight);
            instinctsNbt.appendTag(entryNbt);
        }
        nbtCompound.setTag(INSTINCTS, instinctsNbt);
        
        return nbtCompound;
    }

    @Override
    public void readNBT(Capability<ICapabilityInstinct> capability, ICapabilityInstinct instance, EnumFacing side,
            NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        
        instance.setInstinct(nbtCompound.getFloat(INSTINCT));
        instance.setInactiveInstinctTime(nbtCompound.getInteger(INACTIVE_INSTINCT_TIME));
        
        if (nbtCompound.hasKey(ACTIVE_INSTINCT) && nbtCompound.getString(ACTIVE_INSTINCT) != "") {
            instance.setActiveInstinctId(new ResourceLocation(nbtCompound.getString(ACTIVE_INSTINCT)));
        }
        else {
            instance.setActiveInstinctId(null);
        }
        
        Map<ResourceLocation, InstinctEntry> instinctMap = instance.getInstinctMap();
        instinctMap.clear();
        NBTTagList instinctsNbt = (NBTTagList)nbtCompound.getTag(INSTINCTS);
        if (instinctsNbt != null) {
            int instinctCount = instinctsNbt.tagCount();
            for (int i = 0; i < instinctCount; i++) {
                NBTTagCompound entryNbt = instinctsNbt.getCompoundTagAt(i);
                InstinctEntry entry = new InstinctEntry();
                if (!entryNbt.hasKey(INSTINCT_DATA)) {
                    continue;
                }
                entry.id = new ResourceLocation(entryNbt.getString(INSTINCT_ID));
                try {
                    IInstinct instinct = Instincts.REGISTRY.getValue(entry.id).createInstinct();
                    instinct.deserializeNBT(entryNbt.getCompoundTag(INSTINCT_DATA));
                    entry.instinct = instinct;
                }
                catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
                entry.weight = entryNbt.getFloat(INSTINCT_WEIGHT);
                
                if (entry.instinct != null) {
                    instinctMap.put(entry.id, entry);
                }
            }
        }
    }

}
