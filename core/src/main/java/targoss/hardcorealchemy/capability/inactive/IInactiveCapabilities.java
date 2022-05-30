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

package targoss.hardcorealchemy.capability.inactive;

import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Provides a way to store copies of capability data to be re-applied
 * to the player later.
 */
public interface IInactiveCapabilities {
    public static class Cap {
        public boolean persistsOnDeath = false;
        public NBTTagCompound data = null;
    }
    
    void setCapabilityMap(ConcurrentMap<String, Cap> capabilityMap);
    
    @Nonnull ConcurrentMap<String, Cap> getCapabilityMap();
}
