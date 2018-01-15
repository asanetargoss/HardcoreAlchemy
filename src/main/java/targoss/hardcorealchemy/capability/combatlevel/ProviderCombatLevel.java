/*
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

package targoss.hardcorealchemy.capability.combatlevel;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;

public class ProviderCombatLevel implements ICapabilitySerializable<NBTBase> {
    
    @CapabilityInject(ICapabilityCombatLevel.class)
    public static final Capability<ICapabilityCombatLevel> COMBAT_LEVEL_CAPABILITY = null;
    
    public final ICapabilityCombatLevel instance;
    
    public ProviderCombatLevel() {
        this.instance = COMBAT_LEVEL_CAPABILITY.getDefaultInstance();
    }
    
    public ProviderCombatLevel(int combatLevel) {
        this.instance = COMBAT_LEVEL_CAPABILITY.getDefaultInstance();
        instance.setValue(combatLevel);
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == COMBAT_LEVEL_CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == COMBAT_LEVEL_CAPABILITY) {
            return (T)instance;
        }
        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return COMBAT_LEVEL_CAPABILITY.getStorage().writeNBT(COMBAT_LEVEL_CAPABILITY, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        COMBAT_LEVEL_CAPABILITY.getStorage().readNBT(COMBAT_LEVEL_CAPABILITY, instance, null, nbt);
    }

}
