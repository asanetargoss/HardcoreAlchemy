/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.capability.serverdata;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class StorageServerData implements Capability.IStorage<ICapabilityServerData> {
    private static final String HAS_DIFFICULTY = "hasDifficulty";

    @Override
    public NBTBase writeNBT(Capability<ICapabilityServerData> capability, ICapabilityServerData instance, EnumFacing side) {
        NBTTagCompound nbtCompound = new NBTTagCompound();
        
        nbtCompound.setBoolean(HAS_DIFFICULTY, instance.getHasDifficulty());
        
        return nbtCompound;
    }

    @Override
    public void readNBT(Capability<ICapabilityServerData> capability, ICapabilityServerData instance, EnumFacing side,
            NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        
        if (nbtCompound.hasKey(HAS_DIFFICULTY)) {
            instance.setHasDifficulty(nbtCompound.getBoolean(HAS_DIFFICULTY));
        }
    }

}
