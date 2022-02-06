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

package targoss.hardcorealchemy.tweaks.listener;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.capability.VirtualCapabilityManager;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.tweaks.capability.serverdata.CapabilityServerData;
import targoss.hardcorealchemy.tweaks.capability.serverdata.ICapabilityServerData;
import targoss.hardcorealchemy.tweaks.capability.serverdata.ProviderServerData;
import targoss.hardcorealchemy.tweaks.capability.serverdata.StorageServerData;

/**
 * Use a capability stored in the overworld to check if
 * the difficulty level has been set to hard yet. (We
 * only set it once because we want it to be like the
 * "default" difficulty)
 */
public class ListenerWorldDifficulty extends HardcoreAlchemyListener {
    @Override
    public void registerCapabilities(CapabilityManager manager, VirtualCapabilityManager virtualManager) {
        manager.register(ICapabilityServerData.class, new StorageServerData(), CapabilityServerData.class);
    }
    
    @Override
    public void serverStarting(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        World world = server.worldServerForDimension(DimensionType.OVERWORLD.getId());
        if (world.hasCapability(ProviderServerData.SERVER_DATA_CAPABILITY, null)) {
            ICapabilityServerData worldCap = world.getCapability(ProviderServerData.SERVER_DATA_CAPABILITY, null);
            if (!worldCap.getHasDifficulty()) {
                server.setDifficultyForAllWorlds(EnumDifficulty.HARD);
                worldCap.setHasDifficulty(true);
            }
        }
    }
    
    @SubscribeEvent
    public void onWorldCapability(AttachCapabilitiesEvent<World> event) {
        World world = event.getObject();
        MinecraftServer server = world.getMinecraftServer();
        if (server != null &&
                server.worldServerForDimension(DimensionType.OVERWORLD.getId()) == world) {
            event.addCapability(CapabilityServerData.RESOURCE_LOCATION, new ProviderServerData());
        }
    }
}
