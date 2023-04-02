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
        nbt.setDouble("magicInhibition", instance.getMagicInhibition());
        nbt.setBoolean("isHumanFormInPhylactery", instance.getIsHumanFormInPhylactery());
        nbt.setBoolean("hasForgottenHumanForm", instance.getHasForgottenHumanForm());
        nbt.setBoolean("hasLostHumanity", instance.getHasLostHumanity());
        nbt.setBoolean("hasForgottenMorphAbility", instance.getHasForgottenMorphAbility());
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
        instance.setMagicInhibition(nbtCompound.getDouble("magicInhibition"));
        instance.setIsHumanFormInPhylactery(nbtCompound.getBoolean("isHumanFormInPhylactery"));
        instance.setHasForgottenHumanForm(nbtCompound.getBoolean("hasForgottenHumanForm"));
        instance.setHasLostHumanity(nbtCompound.getBoolean("hasLostHumanity"));
        instance.setHasForgottenMorphAbility(nbtCompound.getBoolean("hasForgottenMorphAbility"));
    }
    
}
