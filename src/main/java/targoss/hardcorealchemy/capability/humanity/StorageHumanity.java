/**
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

package targoss.hardcorealchemy.capability.humanity;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class StorageHumanity implements Capability.IStorage<ICapabilityHumanity> {

    @Override
    public NBTBase writeNBT(Capability<ICapabilityHumanity> capability, ICapabilityHumanity instance, EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setDouble("humanity", instance.getHumanity());
        nbt.setDouble("lastHumanity", instance.getLastHumanity());
        nbt.setBoolean("hasLostHumanity", instance.getHasLostHumanity());
        nbt.setBoolean("hasLostMorphAbility", instance.getHasLostMorphAbility());
        nbt.setBoolean("isMarried", instance.getIsMarried());
        nbt.setBoolean("isMage", instance.getIsMage());
        nbt.setBoolean("highMagicOverride", instance.getHighMagicOverride());
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityHumanity> capability, ICapabilityHumanity instance, EnumFacing side,
            NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        instance.setHumanity(nbtCompound.getDouble("humanity"));
        instance.setLastHumanity(nbtCompound.getDouble("lastHumanity"));
        instance.setHasLostHumanity(nbtCompound.getBoolean("hasLostHumanity"));
        instance.setHasLostMorphAbility(nbtCompound.getBoolean("hasLostMorphAbility"));
        instance.setIsMarried(nbtCompound.getBoolean("isMarried"));
        instance.setIsMage(nbtCompound.getBoolean("isMage"));
        instance.setHighMagicOverride(nbtCompound.getBoolean("highMagicOverride"));
    }
    
}
