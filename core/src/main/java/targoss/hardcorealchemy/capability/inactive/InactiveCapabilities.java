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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

public class InactiveCapabilities implements IInactiveCapabilities {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "inactive_capabilities");
    
    private ConcurrentMap<String, Cap> capabilityMap = new ConcurrentHashMap<String, Cap>(); 

    @Override
    public void setCapabilityMap(ConcurrentMap<String, Cap> capabilityMap) {
        this.capabilityMap = capabilityMap;
    }

    @Override
    public ConcurrentMap<String, Cap> getCapabilityMap() {
        return this.capabilityMap;
    }

}
