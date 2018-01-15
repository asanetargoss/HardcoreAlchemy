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
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import targoss.hardcorealchemy.capability.combatlevel.CapabilityCombatLevel;
import targoss.hardcorealchemy.capability.food.CapabilityFood;
import targoss.hardcorealchemy.capability.humanity.CapabilityHumanity;
import targoss.hardcorealchemy.capability.killcount.CapabilityKillCount;
import targoss.hardcorealchemy.capability.serverdata.CapabilityServerData;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.listener.ConfiguredListener;
import targoss.hardcorealchemy.listener.ListenerBlock;
import targoss.hardcorealchemy.listener.ListenerCrops;
import targoss.hardcorealchemy.listener.ListenerInventoryFoodRot;
import targoss.hardcorealchemy.listener.ListenerMobAI;
import targoss.hardcorealchemy.listener.ListenerMobLevel;
import targoss.hardcorealchemy.listener.ListenerPacketUpdatePlayer;
import targoss.hardcorealchemy.listener.ListenerPlayerDiet;
import targoss.hardcorealchemy.listener.ListenerPlayerHumanity;
import targoss.hardcorealchemy.listener.ListenerPlayerMagic;
import targoss.hardcorealchemy.listener.ListenerPlayerMorphs;
import targoss.hardcorealchemy.listener.ListenerWorldDifficulty;
import targoss.hardcorealchemy.network.PacketHandler;

public class CommonProxy {
    public Configs configs = new Configs();
    
    public static final ImmutableList<Class<? extends ConfiguredListener>> LISTENER_TYPES = ImmutableList.of(
                ListenerPacketUpdatePlayer.class,
                ListenerPlayerMorphs.class,
                ListenerPlayerHumanity.class,
                ListenerPlayerMagic.class,
                ListenerPlayerDiet.class,
                ListenerMobLevel.class,
                ListenerMobAI.class,
                ListenerBlock.class,
                ListenerInventoryFoodRot.class,
                ListenerWorldDifficulty.class,
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
    
    public static final void registerListeners(Collection<ConfiguredListener> listeners) {
        for (ConfiguredListener listener : listeners) {
            MinecraftForge.EVENT_BUS.register(listener);
        }
    }
    
    public static final void registerCapabilities() {
        CapabilityKillCount.register();
        CapabilityHumanity.register();
        CapabilityCombatLevel.register();
        CapabilityFood.register();
        CapabilityServerData.register();
    }
    
    public static final void registerNetworking() {
        PacketHandler.register();
    }
    
    public void preInit(FMLPreInitializationEvent event) {
        for (ConfiguredListener listener : listeners.values()) {
            listener.preInit(event);
        }
    }
    
    public void init(FMLInitializationEvent event) {
        for (ConfiguredListener listener : listeners.values()) {
            listener.init(event);
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
