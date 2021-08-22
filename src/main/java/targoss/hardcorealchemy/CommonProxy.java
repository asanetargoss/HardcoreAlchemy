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

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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
import targoss.hardcorealchemy.capability.combatlevel.CapabilityCombatLevel;
import targoss.hardcorealchemy.capability.combatlevel.ICapabilityCombatLevel;
import targoss.hardcorealchemy.capability.combatlevel.StorageCombatLevel;
import targoss.hardcorealchemy.capability.dimensionhistory.CapabilityDimensionHistory;
import targoss.hardcorealchemy.capability.dimensionhistory.ICapabilityDimensionHistory;
import targoss.hardcorealchemy.capability.dimensionhistory.ProviderDimensionHistory;
import targoss.hardcorealchemy.capability.dimensionhistory.StorageDimensionHistory;
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
import targoss.hardcorealchemy.capability.instinct.CapabilityInstinct;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.capability.instinct.StorageInstinct;
import targoss.hardcorealchemy.capability.killcount.CapabilityKillCount;
import targoss.hardcorealchemy.capability.killcount.ICapabilityKillCount;
import targoss.hardcorealchemy.capability.killcount.StorageKillCount;
import targoss.hardcorealchemy.capability.misc.CapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.StorageMisc;
import targoss.hardcorealchemy.capability.morphstate.CapabilityMorphState;
import targoss.hardcorealchemy.capability.morphstate.ICapabilityMorphState;
import targoss.hardcorealchemy.capability.morphstate.StorageMorphState;
import targoss.hardcorealchemy.capability.research.CapabilityResearch;
import targoss.hardcorealchemy.capability.research.ICapabilityResearch;
import targoss.hardcorealchemy.capability.research.StorageResearch;
import targoss.hardcorealchemy.capability.serverdata.CapabilityServerData;
import targoss.hardcorealchemy.capability.serverdata.ICapabilityServerData;
import targoss.hardcorealchemy.capability.serverdata.StorageServerData;
import targoss.hardcorealchemy.capability.tilehistory.CapabilityTileHistory;
import targoss.hardcorealchemy.capability.tilehistory.ICapabilityTileHistory;
import targoss.hardcorealchemy.capability.tilehistory.StorageTileHistory;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.entity.Entities;
import targoss.hardcorealchemy.incantation.Incantations;
import targoss.hardcorealchemy.instinct.Instincts;
import targoss.hardcorealchemy.item.Items;
import targoss.hardcorealchemy.listener.ConfiguredListener;
import targoss.hardcorealchemy.listener.ListenerConfigs;
import targoss.hardcorealchemy.listener.ListenerCrops;
import targoss.hardcorealchemy.listener.ListenerEntityCapabilities;
import targoss.hardcorealchemy.listener.ListenerEntityVoidfade;
import targoss.hardcorealchemy.listener.ListenerInstinctOverheat;
import targoss.hardcorealchemy.listener.ListenerInventoryFoodRot;
import targoss.hardcorealchemy.listener.ListenerMobAI;
import targoss.hardcorealchemy.listener.ListenerMobLevel;
import targoss.hardcorealchemy.listener.ListenerPlayerDiet;
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
import targoss.hardcorealchemy.listener.ListenerWorldDifficulty;
import targoss.hardcorealchemy.metamorph.HcAMetamorphPack;
import targoss.hardcorealchemy.modpack.guide.AlchemicAshGuide;
import targoss.hardcorealchemy.modpack.guide.HCAModpackGuide;
import targoss.hardcorealchemy.modpack.guide.HCAUpgradeGuides;
import targoss.hardcorealchemy.network.PacketHandler;
import targoss.hardcorealchemy.registrar.RegistrarUpgradeGuide;
import targoss.hardcorealchemy.research.Studies;

public class CommonProxy {
    public Configs configs = new Configs();
    
    @SuppressWarnings("unchecked")
    public static final ImmutableList<Class<? extends ConfiguredListener>> LISTENER_TYPES = ImmutableList.of(
                ListenerEntityCapabilities.class,
                ListenerPlayerMorphs.class,
                ListenerPlayerHumanity.class,
                ListenerPlayerMagic.class,
                ListenerPlayerDiet.class,
                ListenerMobLevel.class,
                ListenerMobAI.class,
                ListenerSmallTweaks.class,
                ListenerInventoryFoodRot.class,
                ListenerWorldDifficulty.class,
                ListenerPlayerMagicState.class,
                ListenerPlayerMorphState.class,
                ListenerPlayerInstinct.class,
                ListenerPlayerHinderedMind.class,
                ListenerPlayerIncantation.class,
                ListenerPlayerInventory.class,
                ListenerPlayerResearch.class,
                ListenerConfigs.class,
                ListenerInstinctOverheat.class,
                ListenerEntityVoidfade.class,
                ListenerCrops.class // 1.10-specific
            );
    
    public ImmutableList<Class<? extends ConfiguredListener>> getListenerTypes() {
        return LISTENER_TYPES;
    }
    
    public final ImmutableMap<Class<? extends ConfiguredListener>, ConfiguredListener> listeners;
    
    public CommonProxy() {
        // Initialize listeners with CommonProxy.configs as the parameter
        Map<Class<? extends ConfiguredListener>, ConfiguredListener> listenerBuilder = new HashMap<>();
        
        for (Class<? extends ConfiguredListener> listenerClass : getListenerTypes()) {
            try {
                listenerBuilder.put(listenerClass, listenerClass.getConstructor(Configs.class).newInstance(configs));
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }
        
        listeners = ImmutableMap.copyOf(listenerBuilder);
    }
    
    public void registerListeners(Collection<ConfiguredListener> listeners) {
        for (ConfiguredListener listener : listeners) {
            MinecraftForge.EVENT_BUS.register(listener);
        }
    }
    
    public void registerCapabilities() {
        CapabilityManager.INSTANCE.register(ICapabilityKillCount.class, new StorageKillCount(), CapabilityKillCount.class);
        CapabilityManager.INSTANCE.register(ICapabilityHumanity.class, new StorageHumanity(), CapabilityHumanity.class);
        CapabilityManager.INSTANCE.register(ICapabilityCombatLevel.class, new StorageCombatLevel(), CapabilityCombatLevel.class);
        CapabilityManager.INSTANCE.register(ICapabilityFood.class, new StorageFood(), CapabilityFood.class);
        CapUtil.registerVirtualCapability(CapabilityFood.RESOURCE_LOCATION, CapabilityFood.FOOD_CAPABILITY);
        CapabilityManager.INSTANCE.register(ICapabilityServerData.class, new StorageServerData(), CapabilityServerData.class);
        CapabilityManager.INSTANCE.register(IInactiveCapabilities.class, new StorageInactiveCapabilities(), InactiveCapabilities.class);
        CapabilityManager.INSTANCE.register(ICapabilityMorphState.class, new StorageMorphState(), CapabilityMorphState.class);
        CapabilityManager.INSTANCE.register(ICapabilityInstinct.class, new StorageInstinct(), CapabilityInstinct.class);
        CapabilityManager.INSTANCE.register(ICapabilityMisc.class, new StorageMisc(), CapabilityMisc.class);
        CapabilityManager.INSTANCE.register(ICapabilityEntityState.class, new StorageEntityState(), CapabilityEntityState.class);
        CapabilityManager.INSTANCE.register(ICapabilityTileHistory.class, new StorageTileHistory(), CapabilityTileHistory.class);
        CapabilityManager.INSTANCE.register(ICapabilityResearch.class, new StorageResearch(), CapabilityResearch.class);
        CapabilityManager.INSTANCE.register(ICapabilityDimensionHistory.class, new StorageDimensionHistory(), CapabilityDimensionHistory.class);
        CapUtil.registerVirtualCapability(CapabilityDimensionHistory.RESOURCE_LOCATION, ProviderDimensionHistory.DIMENSION_HISTORY_CAPABILITY);
    }
    
    public void registerNetworking() {
        PacketHandler.register();
    }
    
    public void preInit(FMLPreInitializationEvent event) {
        for (ConfiguredListener listener : listeners.values()) {
            listener.preInit(event);
        }
        
        Items.ITEMS.register();
        Items.POTIONS.register();
        Items.POTION_TYPES.register();
        Entities.ENTITIES.register();
        Studies.KNOWLEDGE_FACTS.register();
        
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
        registerListeners(listeners.values());
        registerCapabilities();
        
        for (ConfiguredListener listener : listeners.values()) {
            listener.init(event);
        }
        
        Items.registerRecipes();
        HcAMetamorphPack.registerAbilities();
        // Why are these in init and not pre-init?
        Instincts.INSTINCTS.register();
        Instincts.INSTINCT_NEED_FACTORIES.register();
        Instincts.INSTINCT_EFFECTS.register();
        Incantations.INCANTATIONS.register();
        
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
        for (ConfiguredListener listener : listeners.values()) {
            listener.postInit(event);
        }
    }
    
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        for (ConfiguredListener listener : listeners.values()) {
            listener.serverAboutToStart(event);
        }
    }
    
    public void serverStarting(FMLServerStartingEvent event) {
        for (ConfiguredListener listener : listeners.values()) {
            listener.serverStarting(event);
        }
    }
    
    public void serverStarted(FMLServerStartedEvent event) {
        for (ConfiguredListener listener : listeners.values()) {
            listener.serverStarted(event);
        }
    }
    
    public void serverStopping(FMLServerStoppingEvent event) {
        for (ConfiguredListener listener : listeners.values()) {
            listener.serverStopping(event);
        }
    }
}
