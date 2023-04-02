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

package targoss.hardcorealchemy.listener;

import net.minecraftforge.common.capabilities.CapabilityManager;
import targoss.hardcorealchemy.capability.VirtualCapabilityManager;
import targoss.hardcorealchemy.capability.entitystate.CapabilityEntityState;
import targoss.hardcorealchemy.capability.entitystate.ICapabilityEntityState;
import targoss.hardcorealchemy.capability.entitystate.StorageEntityState;
import targoss.hardcorealchemy.capability.food.CapabilityFood;
import targoss.hardcorealchemy.capability.food.ICapabilityFood;
import targoss.hardcorealchemy.capability.food.StorageFood;
import targoss.hardcorealchemy.capability.humanity.CapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.StorageHumanity;
import targoss.hardcorealchemy.capability.inactive.IInactiveCapabilities;
import targoss.hardcorealchemy.capability.inactive.InactiveCapabilities;
import targoss.hardcorealchemy.capability.inactive.StorageInactiveCapabilities;
import targoss.hardcorealchemy.capability.misc.CapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.StorageMisc;
import targoss.hardcorealchemy.capability.research.CapabilityResearch;
import targoss.hardcorealchemy.capability.research.ICapabilityResearch;
import targoss.hardcorealchemy.capability.research.StorageResearch;
import targoss.hardcorealchemy.capability.tilehistory.CapabilityTileHistory;
import targoss.hardcorealchemy.capability.tilehistory.ICapabilityTileHistory;
import targoss.hardcorealchemy.capability.tilehistory.StorageTileHistory;

public class ListenerCapabilities extends HardcoreAlchemyListener {
    
    @Override
    public void registerCapabilities(CapabilityManager manager, VirtualCapabilityManager virtualManager) {
        manager.register(ICapabilityHumanity.class, new StorageHumanity(), CapabilityHumanity.class);
        manager.register(ICapabilityFood.class, new StorageFood(), CapabilityFood.class);
        virtualManager.registerVirtualCapability(CapabilityFood.RESOURCE_LOCATION, CapabilityFood.FOOD_CAPABILITY);
        manager.register(IInactiveCapabilities.class, new StorageInactiveCapabilities(), InactiveCapabilities.class);
        manager.register(ICapabilityMisc.class, new StorageMisc(), CapabilityMisc.class);
        manager.register(ICapabilityEntityState.class, new StorageEntityState(), CapabilityEntityState.class);
        manager.register(ICapabilityTileHistory.class, new StorageTileHistory(), CapabilityTileHistory.class);
        manager.register(ICapabilityResearch.class, new StorageResearch(), CapabilityResearch.class);
    }

}
