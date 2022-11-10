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

import java.util.ArrayList;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import targoss.hardcorealchemy.util.WorldUtil;

/**
 * Handles the full lifecycle for global server data
 */
public class UniverseCapabilityManager {
    public static UniverseCapabilityManager INSTANCE = new UniverseCapabilityManager(CapabilityManager.INSTANCE);
    
    protected CapabilityManager forgeCaps;
    
    protected static class UniverseCap {
        public ResourceLocation key;
        public ICapabilityProvider cap;
        
        public UniverseCap(ResourceLocation key, ICapabilityProvider cap) {
            this.key = key;
            this.cap = cap;
        }
    }
    protected ArrayList<UniverseCap> universeCaps;
    
    public UniverseCapabilityManager(CapabilityManager forgeCaps) {
        this.forgeCaps = forgeCaps;
    }
    
    /** Capabilities registered here will automatically be attached via the attachCapabilitiesEvent */
    public void register(ResourceLocation key, ICapabilityProvider cap) {
        UniverseCap ucap = new UniverseCap(key, cap);
        universeCaps.add(ucap);
    }
    
    public void maybeAttachCapabilities(AttachCapabilitiesEvent<World> event) {
        World eventWorld = event.getObject();
        World overworld = WorldUtil.getOverworld(eventWorld);
        if (eventWorld != overworld) {
            return;
        }
        for (UniverseCap ucap : universeCaps) {
            event.addCapability(ucap.key, ucap.cap);
        }
    }

    public <T> T get(World dummyWorld, Capability<T> capability) {
        World overworld = WorldUtil.getOverworld(dummyWorld);
        T instance = overworld.getCapability(capability, null);
        return instance;
    }
}
