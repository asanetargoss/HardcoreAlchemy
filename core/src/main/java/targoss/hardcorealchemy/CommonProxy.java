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

package targoss.hardcorealchemy;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.MinecraftForge;
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
import targoss.hardcorealchemy.entity.Entities;
import targoss.hardcorealchemy.incantation.Incantations;
import targoss.hardcorealchemy.item.Items;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.listener.ListenerCapabilities;
import targoss.hardcorealchemy.listener.ListenerConfigs;
import targoss.hardcorealchemy.listener.ListenerEntityCapabilities;
import targoss.hardcorealchemy.listener.ListenerPlayerIncantation;
import targoss.hardcorealchemy.listener.ListenerPlayerResearch;
import targoss.hardcorealchemy.network.MessageConfigs;
import targoss.hardcorealchemy.network.MessageInactiveCapabilities;
import targoss.hardcorealchemy.network.NetMessenger;
import targoss.hardcorealchemy.network.RequestIncantation;
import targoss.hardcorealchemy.research.Studies;

public class CommonProxy {
    public Configs configs = new Configs();
    
    protected List<HardcoreAlchemyListener> listeners = new ArrayList<>();
    
    public NetMessenger<HardcoreAlchemy> messenger;
    
    public void addListener(HardcoreAlchemyListener listener) {
        listener.setConfigs(configs);
        listeners.add(listener);
    }
    
    public CommonProxy() {
        addListener(new ListenerCapabilities());
        addListener(new ListenerEntityCapabilities());
        addListener(new ListenerPlayerIncantation());
        addListener(new ListenerPlayerResearch());
        addListener(new ListenerConfigs());
    }
    
    public void registerNetworking() {
        messenger = new NetMessenger<HardcoreAlchemy>(HardcoreAlchemy.MOD_ID)
            .register(new MessageInactiveCapabilities())
            .register(new MessageConfigs())
            .register(new RequestIncantation());
    }
    
    public void preInit(FMLPreInitializationEvent event) {
        for (HardcoreAlchemyListener listener : listeners) {
            listener.preInit(event);
        }
        
        Items.ITEMS.register();
        Items.POTIONS.register();
        Items.POTION_TYPES.register();
        Entities.ENTITIES.register();
        Studies.KNOWLEDGE_FACTS.register();
        // asanetargoss @ 2021-10-03: Moved incantation registration from init to preInit
        Incantations.INCANTATIONS.register();
        
        registerNetworking();
    }
    
    public void init(FMLInitializationEvent event) {
        for (HardcoreAlchemyListener listener : listeners) {
            MinecraftForge.EVENT_BUS.register(listener);
        }
        for (HardcoreAlchemyListener listener : listeners) {
            listener.registerCapabilities(CapabilityManager.INSTANCE, CapUtil.Manager.INSTANCE);
        }
        for (HardcoreAlchemyListener listener : listeners) {
            listener.init(event);
        }
        
        Items.registerRecipes();
    }
    
    public void postInit(FMLPostInitializationEvent event) {
        for (HardcoreAlchemyListener listener : listeners) {
            listener.postInit(event);
        }
    }
    
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        for (HardcoreAlchemyListener listener : listeners) {
            listener.serverAboutToStart(event);
        }
    }
    
    public void serverStarting(FMLServerStartingEvent event) {
        for (HardcoreAlchemyListener listener : listeners) {
            listener.serverStarting(event);
        }
    }
    
    public void serverStarted(FMLServerStartedEvent event) {
        for (HardcoreAlchemyListener listener : listeners) {
            listener.serverStarted(event);
        }
    }
    
    public void serverStopping(FMLServerStoppingEvent event) {
        for (HardcoreAlchemyListener listener : listeners) {
            listener.serverStopping(event);
        }
    }
}
