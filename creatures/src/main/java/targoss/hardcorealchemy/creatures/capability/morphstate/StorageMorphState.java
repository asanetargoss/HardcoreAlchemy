/*
 * Copyright 2017-2022 asanetargoss
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

package targoss.hardcorealchemy.creatures.capability.morphstate;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class StorageMorphState implements Capability.IStorage<ICapabilityMorphState> {
    public static final String IS_FISHING_UNDERWATER = "isFishingUnderwater";
    public static final String FISHING_TIMER = "fishingTimer";

    @Override
    public NBTBase writeNBT(Capability<ICapabilityMorphState> capability, ICapabilityMorphState instance, EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        
        nbt.setBoolean(IS_FISHING_UNDERWATER, instance.getIsFishingUnderwater());
        nbt.setInteger(FISHING_TIMER, instance.getFishingTimer());
        
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityMorphState> capability, ICapabilityMorphState instance, EnumFacing side, NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        
        instance.setIsFishingUnderwater(nbtCompound.getBoolean(IS_FISHING_UNDERWATER));
        instance.setFishingTimer(nbtCompound.getInteger(FISHING_TIMER));
    }

}
