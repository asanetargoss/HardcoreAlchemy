/*
 * Copyright 2017-2022 asanetargoss
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

package targoss.hardcorealchemy.capability.misc;

import java.util.UUID;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class StorageMisc implements IStorage<ICapabilityMisc> {
    private static final String LIFETIME_UUID = "lifetimeUUID";
    private static final String LAST_LOGIN_VERSION = "lastLoginVersion";
    @Override
    public NBTBase writeNBT(Capability<ICapabilityMisc> capability, ICapabilityMisc instance, EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        
        {
            UUID uuid = instance.getLifetimeUUID();
            if (uuid != null) {
                nbt.setString(LIFETIME_UUID, uuid.toString());
            }
        }
        
        if (instance.getLastLoginVersion() != null) {
            nbt.setString(LAST_LOGIN_VERSION, instance.getLastLoginVersion());
        }
        
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityMisc> capability, ICapabilityMisc instance, EnumFacing side, NBTBase nbtBase) {
        if (!(nbtBase instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbt = (NBTTagCompound)nbtBase;
        
        if (nbt.hasKey(LIFETIME_UUID)) {
            try {
                UUID uuid = UUID.fromString(nbt.getString(LIFETIME_UUID));
                instance.setLifetimeUUID(uuid);
            } catch (IllegalArgumentException e) {}
        }
        
        if (nbt.hasKey(LAST_LOGIN_VERSION)) {
            instance.setLastLoginVersion(nbt.getString(LAST_LOGIN_VERSION));
        }
    }
}
