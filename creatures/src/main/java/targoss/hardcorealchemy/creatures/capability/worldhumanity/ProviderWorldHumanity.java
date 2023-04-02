/*
 * Copyright 2017-2023 asanetargoss
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

package targoss.hardcorealchemy.creatures.capability.worldhumanity;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ProviderWorldHumanity implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(ICapabilityWorldHumanity.class)
    public static final Capability<ICapabilityWorldHumanity> WORLD_HUMANITY_CAPABILITY = null;
    
    protected ICapabilityWorldHumanity instance = new CapabilityWorldHumanity();

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == WORLD_HUMANITY_CAPABILITY;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == WORLD_HUMANITY_CAPABILITY) {
            return (T)instance;
        }
        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return WORLD_HUMANITY_CAPABILITY.getStorage().writeNBT(WORLD_HUMANITY_CAPABILITY, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        WORLD_HUMANITY_CAPABILITY.getStorage().readNBT(WORLD_HUMANITY_CAPABILITY, instance, null, nbt);
    }

}
