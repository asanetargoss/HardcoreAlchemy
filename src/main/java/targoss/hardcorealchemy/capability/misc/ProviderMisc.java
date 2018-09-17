/*
 * Copyright 2018 asanetargoss
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

package targoss.hardcorealchemy.capability.misc;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ProviderMisc implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(ICapabilityMisc.class)
    public static final Capability<ICapabilityMisc> MISC_CAPABILITY = null;
    
    private final ICapabilityMisc instance = MISC_CAPABILITY.getDefaultInstance();
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == MISC_CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == MISC_CAPABILITY) {
            return (T)instance;
        }
        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return MISC_CAPABILITY.writeNBT(instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        MISC_CAPABILITY.readNBT(instance, null, nbt);
    }

}
