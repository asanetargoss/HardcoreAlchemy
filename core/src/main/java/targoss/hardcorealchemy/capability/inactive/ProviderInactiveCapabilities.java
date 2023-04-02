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

package targoss.hardcorealchemy.capability.inactive;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ProviderInactiveCapabilities implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(IInactiveCapabilities.class)
    public static final Capability<IInactiveCapabilities> INACTIVE_CAPABILITIES = null;
    
    public final IInactiveCapabilities instance;
    
    public ProviderInactiveCapabilities() {
        instance = INACTIVE_CAPABILITIES.getDefaultInstance();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == INACTIVE_CAPABILITIES;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == INACTIVE_CAPABILITIES) {
            return (T)instance;
        }
        
        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return INACTIVE_CAPABILITIES.getStorage().writeNBT(INACTIVE_CAPABILITIES, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        INACTIVE_CAPABILITIES.getStorage().readNBT(INACTIVE_CAPABILITIES, instance, null, nbt);
    }

}
