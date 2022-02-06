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

package targoss.hardcorealchemy.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class CapUtil {
    /**
     * Copy pretty much any capability by taking advantage of NBT serialization
     * Returns true if successful
     */
    public static <T,V extends ICapabilityProvider,W extends ICapabilityProvider> boolean
            copyOldToNew(Capability<T> capability, V oldProvider, W newProvider) {
        T oldCapability = oldProvider.getCapability(capability, null);
        if (oldCapability == null) {
            return false;
        }
        T newCapability = newProvider.getCapability(capability, null);
        if (newCapability == null) {
            return false;
        }
        IStorage<T> storage = capability.getStorage();
        NBTBase oldNBT = storage.writeNBT(capability, oldCapability, null);
        storage.readNBT(capability, newCapability, null, oldNBT);
        return true;
    }
}
