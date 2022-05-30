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

package targoss.hardcorealchemy.capability.entitystate;

import java.util.UUID;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class StorageEntityState implements Capability.IStorage<ICapabilityEntityState> {
    private static final String TARGET_PLAYER_ID = "targetPlayerID";
    private static final String AGE = "age";
    private static final String LIFETIME = "lifetime";
    
    @Override
    public NBTBase writeNBT(Capability<ICapabilityEntityState> capability, ICapabilityEntityState instance,
            EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        
        UUID playerID = instance.getTargetPlayerID();
        if (playerID != null) {
            nbt.setString(TARGET_PLAYER_ID, playerID.toString());
        }
        
        nbt.setInteger(AGE, instance.getAge());
        nbt.setInteger(LIFETIME, instance.getLifetime());
        
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityEntityState> capability, ICapabilityEntityState instance, EnumFacing side,
            NBTBase nbtBase) {
        if (!(nbtBase instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbt = (NBTTagCompound)nbtBase;
        
        if (nbt.hasKey(TARGET_PLAYER_ID)) {
            try {
                UUID uuid = UUID.fromString(nbt.getString(TARGET_PLAYER_ID));
                instance.setTargetPlayerID(uuid);
            } catch (IllegalArgumentException e) {}
        }
        
        instance.setAge(nbt.getInteger(AGE));
        if (nbt.hasKey(LIFETIME)) {
            instance.setLifetime(nbt.getInteger(LIFETIME));
        }
    }

}
