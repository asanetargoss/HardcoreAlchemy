/*
 * Copyright 2021 asanetargoss
 * 
 * This file is part of the Hardcore Alchemy capstone mod.
 * 
 * The Hardcore Alchemy capstone mod is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3 of the
 * License.
 * 
 * The Hardcore Alchemy capstone mod is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Hardcore Alchemy capstone mod. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.capability.dimensionhistory;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ProviderDimensionHistory implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(ICapabilityDimensionHistory.class)
    public static final Capability<ICapabilityDimensionHistory> DIMENSION_HISTORY_CAPABILITY = null;
    
    public final ICapabilityDimensionHistory instance;
    
    public ProviderDimensionHistory() {
        this.instance = new CapabilityDimensionHistory();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == DIMENSION_HISTORY_CAPABILITY;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == DIMENSION_HISTORY_CAPABILITY) {
            return (T)instance;
        }
        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return DIMENSION_HISTORY_CAPABILITY.getStorage().writeNBT(DIMENSION_HISTORY_CAPABILITY, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        DIMENSION_HISTORY_CAPABILITY.getStorage().readNBT(DIMENSION_HISTORY_CAPABILITY, instance, null, nbt);
    }

}
