/*
 * Copyright 2021 asanetargoss
 * 
 * This file is part of the Hardcore Alchemy capstone mod.
 * 
 * The Hardcore Alchemy capstone mod is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3 of the
 * License.
 * 
 * The Hardcore Alchemy capstone mod is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Hardcore Alchemy capstone mod. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.capability.dimensionhistory;

import static targoss.hardcorealchemy.util.Serialization.NBT_INT_ARRAY_ID;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import scala.actors.threadpool.Arrays;

public class StorageDimensionHistory implements Capability.IStorage<ICapabilityDimensionHistory> {
    /** For now, this is a non-null array of ints */
    private static final String DIMENSION_HISTORY = "dimensionHistory";

    @Override
    public NBTBase writeNBT(Capability<ICapabilityDimensionHistory> capability, ICapabilityDimensionHistory instance,
            EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        
        List<Integer> dimensionHistory = instance.getDimensionHistory();
        int[] filteredDimensionHistory = new int[dimensionHistory.size()];
        int filteredDimensionHistorySize = 0;
        for (Integer dimension : instance.getDimensionHistory()) {
            if (dimension == null) {
                continue;
            }
            filteredDimensionHistory[filteredDimensionHistorySize++] = dimension;
        }
        if (filteredDimensionHistorySize == dimensionHistory.size()) {
            nbt.setIntArray(DIMENSION_HISTORY, filteredDimensionHistory);
        }
        else if (filteredDimensionHistorySize > 0) {
            nbt.setIntArray(DIMENSION_HISTORY, (int[])Arrays.copyOf(filteredDimensionHistory, filteredDimensionHistorySize));
        }
        
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityDimensionHistory> capability, ICapabilityDimensionHistory instance,
            EnumFacing side, NBTBase nbtBase) {
        if (!(nbtBase instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbt = (NBTTagCompound)nbtBase;
        
        if (nbt.hasKey(DIMENSION_HISTORY, NBT_INT_ARRAY_ID)) {
            int[] historySizeNbt = nbt.getIntArray(DIMENSION_HISTORY);
            List<Integer> dimensionHistory = new ArrayList<>(historySizeNbt.length);
            for (int dimension : historySizeNbt) {
                dimensionHistory.add(dimension);
            }
            instance.setDimensionHistory(dimensionHistory);
        }
    }

}
