/*
 * Copyright 2017-2023 asanetargoss
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

package targoss.hardcorealchemy.capability.food;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import targoss.hardcorealchemy.util.MorphDiet;

public class StorageFood implements Capability.IStorage<ICapabilityFood> {

    @Override
    public NBTBase writeNBT(Capability<ICapabilityFood> capability, ICapabilityFood instance, EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        
        MorphDiet.Restriction restriction = instance.getRestriction();
        String restrictionString = "";
        if (restriction != null) {
            restrictionString = restriction.toString();
        }
        nbt.setString("restriction", restrictionString);
        
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityFood> capability, ICapabilityFood instance, EnumFacing side,
            NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        
        instance.setRestriction(MorphDiet.Restriction.fromString(nbtCompound.getString("restriction")));
    }

}
