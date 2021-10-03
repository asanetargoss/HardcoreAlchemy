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

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.config.Configs;

/**
 * A listener contains Forge events and gameplay logic.
 * It is initialized with a Configs instance.
 */
public abstract class ConfiguredListener {
    public Configs configs;
    
    private ConfiguredListener() { }
    
    public ConfiguredListener(Configs configs) {
        this.configs = configs;
    }
    
    public void preInit(FMLPreInitializationEvent event) { }
    
    public void init(FMLInitializationEvent event) { }
    
    public void postInit(FMLPostInitializationEvent event) { }
    
    public void serverAboutToStart(FMLServerAboutToStartEvent event) { }
    
    public void serverStarting(FMLServerStartingEvent event) { }
    
    public void serverStarted(FMLServerStartedEvent event) { }
    
    public void serverStopping(FMLServerStoppingEvent event) { }
}