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

package targoss.hardcorealchemy.listener;

import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.config.Configs;

/**
 * A listener object that registers things and performs gameplay logic
 * under the Hardcore Alchemy mod ID.
 * It can perform actions at different mod phases for registering
 * items and such.
 * It will also be registered on Forge's event bus, so any functions
 * with @SubscribeEvent will be registered by Forge.
 */
public abstract class HardcoreAlchemyListener {
    public Configs coreConfigs;
    
    public HardcoreAlchemyListener() { }
    
    public final void setConfigs(Configs configs) {
        this.coreConfigs = configs;
    }
    
    public void preInit(FMLPreInitializationEvent event) { }
    
    public void registerCapabilities(CapabilityManager manager, CapUtil.Manager virtualManager) { }
    
    public void init(FMLInitializationEvent event) { }
    
    public void postInit(FMLPostInitializationEvent event) { }
    
    public void serverAboutToStart(FMLServerAboutToStartEvent event) { }
    
    public void serverStarting(FMLServerStartingEvent event) { }
    
    public void serverStarted(FMLServerStartedEvent event) { }
    
    public void serverStopping(FMLServerStoppingEvent event) { }
}