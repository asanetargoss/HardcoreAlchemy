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
import targoss.hardcorealchemy.instinct.Instincts;
import targoss.hardcorealchemy.item.Items;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.listener.ListenerCapabilities;
import targoss.hardcorealchemy.listener.ListenerConfigs;
import targoss.hardcorealchemy.listener.ListenerEntityCapabilities;
import targoss.hardcorealchemy.listener.ListenerInstinctOverheat;
import targoss.hardcorealchemy.listener.ListenerMobAI;
import targoss.hardcorealchemy.listener.ListenerPlayerHinderedMind;
import targoss.hardcorealchemy.listener.ListenerPlayerHumanity;
import targoss.hardcorealchemy.listener.ListenerPlayerIncantation;
import targoss.hardcorealchemy.listener.ListenerPlayerInstinct;
import targoss.hardcorealchemy.listener.ListenerPlayerInventory;
import targoss.hardcorealchemy.listener.ListenerPlayerMagic;
import targoss.hardcorealchemy.listener.ListenerPlayerMagicState;
import targoss.hardcorealchemy.listener.ListenerPlayerMorphState;
import targoss.hardcorealchemy.listener.ListenerPlayerMorphs;
import targoss.hardcorealchemy.listener.ListenerPlayerResearch;
import targoss.hardcorealchemy.listener.ListenerSmallTweaks;
import targoss.hardcorealchemy.metamorph.HcAMetamorphPack;
import targoss.hardcorealchemy.modpack.guide.AlchemicAshGuide;
import targoss.hardcorealchemy.modpack.guide.HCAModpackGuide;
import targoss.hardcorealchemy.modpack.guide.HCAUpgradeGuides;
import targoss.hardcorealchemy.network.PacketHandler;
import targoss.hardcorealchemy.registrar.RegistrarUpgradeGuide;
import targoss.hardcorealchemy.research.Studies;

public class CommonProxy {
    public Configs configs = new Configs();
    
    protected List<HardcoreAlchemyListener> listeners = new ArrayList<>();
    
    public void addListener(HardcoreAlchemyListener listener) {
        listener.setConfigs(configs);
        listeners.add(listener);
    }
    
    public CommonProxy() {
        addListener(new ListenerCapabilities());
        addListener(new ListenerEntityCapabilities());
        addListener(new ListenerPlayerMorphs());
        addListener(new ListenerPlayerHumanity());
        addListener(new ListenerPlayerMagic());
        addListener(new ListenerMobAI());
        addListener(new ListenerSmallTweaks());
        addListener(new ListenerPlayerMagicState());
        addListener(new ListenerPlayerMorphState());
        addListener(new ListenerPlayerInstinct());
        addListener(new ListenerPlayerHinderedMind());
        addListener(new ListenerPlayerIncantation());
        addListener(new ListenerPlayerInventory());
        addListener(new ListenerPlayerResearch());
        addListener(new ListenerConfigs());
        addListener(new ListenerInstinctOverheat());
    }
    
    public void registerNetworking() {
        PacketHandler.register();
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
        // asanetargoss @ 2021-10-03: Moved instinct and incantation registration from init to preInit 
        Instincts.INSTINCTS.register();
        Instincts.INSTINCT_NEED_FACTORIES.register();
        Instincts.INSTINCT_EFFECTS.register();
        Incantations.INCANTATIONS.register();
        
        if (ModState.isGuideapiLoaded) {
            HCAModpackGuide.preInit();
            HCAUpgradeGuides.UPGRADE_GUIDES.register(RegistrarUpgradeGuide.BOOK_AND_MODEL);
        }
        
        if (ModState.isGuideapiLoaded && ModState.isAlchemicAshLoaded) {
            AlchemicAshGuide.preInit();
        }
        
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
        HcAMetamorphPack.registerAbilities();
        
        if (ModState.isGuideapiLoaded) {
            HCAModpackGuide.init();
            HCAUpgradeGuides.UPGRADE_GUIDES.register(RegistrarUpgradeGuide.RECIPES);
            HCAUpgradeGuides.UPGRADE_GUIDES.register(RegistrarUpgradeGuide.CATEGORIES);
            HCAUpgradeGuides.UPGRADE_GUIDES.register(RegistrarUpgradeGuide.CLEANUP);
        }
        
        if (ModState.isGuideapiLoaded && ModState.isAlchemicAshLoaded) {
            AlchemicAshGuide.init();
        }
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
