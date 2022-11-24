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

package targoss.hardcorealchemy.capability;

import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import targoss.hardcorealchemy.util.WorldUtil;

/**
 * Handles the full lifecycle for global server data.
 * Attach universe capabilities by listening to EventUniverseCapabilities .
 * Get universe capabilities by calling UniverseCapabilityManager.get(..) .
 */
public class UniverseCapabilityManager {
    public static UniverseCapabilityManager INSTANCE = new UniverseCapabilityManager(CapabilityManager.INSTANCE);
    
    protected CapabilityManager forgeCaps;
    
    public UniverseCapabilityManager(CapabilityManager forgeCaps) {
        this.forgeCaps = forgeCaps;
    }
    
    public <T> T getCapability(World dummyWorld, Capability<T> capability) {
        World overworld = WorldUtil.getOverworld(dummyWorld);
        T instance = overworld.getCapability(capability, null);
        return instance;
    }
    
    public void maybeAttachCapabilities(AttachCapabilitiesEvent<World> event) {
        World eventWorld = event.getObject();
        World overworld = WorldUtil.getOverworld(eventWorld);
        if (eventWorld != overworld) {
            return;
        }
        MinecraftForge.EVENT_BUS.post(new EventUniverseCapabilities(event));
    }
}
