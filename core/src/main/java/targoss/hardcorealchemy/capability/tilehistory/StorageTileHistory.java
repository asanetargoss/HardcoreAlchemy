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

package targoss.hardcorealchemy.capability.tilehistory;

import java.util.UUID;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class StorageTileHistory implements IStorage<ICapabilityTileHistory> {
    private static final String LIFETIME_UUID = "lifetimeUUID";

    @Override
    public NBTBase writeNBT(Capability<ICapabilityTileHistory> capability, ICapabilityTileHistory instance,
            EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        UUID ownerLifetimeUUID = instance.getOwnerLifetimeUUID();
        if (ownerLifetimeUUID != null) {
            nbt.setString(LIFETIME_UUID, ownerLifetimeUUID.toString());
        }
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityTileHistory> capability, ICapabilityTileHistory instance, EnumFacing side,
            NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        UUID ownerLifetimeUUID = null;
        try {
            ownerLifetimeUUID = UUID.fromString(nbtCompound.getString(LIFETIME_UUID));
        } catch (Exception e) {}
        instance.setOwnerLifetimeUUID(ownerLifetimeUUID);
    }

}
