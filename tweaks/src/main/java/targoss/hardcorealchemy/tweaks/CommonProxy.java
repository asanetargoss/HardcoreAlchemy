/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks;

import static targoss.hardcorealchemy.item.Items.HEARTS;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.network.NetMessenger;
import targoss.hardcorealchemy.tweaks.item.Items;
import targoss.hardcorealchemy.tweaks.listener.ListenerBedBreakHarvest;
import targoss.hardcorealchemy.tweaks.listener.ListenerCraftTimefrozen;
import targoss.hardcorealchemy.tweaks.listener.ListenerEntityVoidfade;
import targoss.hardcorealchemy.tweaks.listener.ListenerHeartShards;
import targoss.hardcorealchemy.tweaks.listener.ListenerHearts;
import targoss.hardcorealchemy.tweaks.listener.ListenerInventoryFoodRot;
import targoss.hardcorealchemy.tweaks.listener.ListenerMobEffect;
import targoss.hardcorealchemy.tweaks.listener.ListenerMobLevel;
import targoss.hardcorealchemy.tweaks.listener.ListenerPlayerShield;
import targoss.hardcorealchemy.tweaks.listener.ListenerPlayerSlip;
import targoss.hardcorealchemy.tweaks.listener.ListenerWorldDifficulty;
import targoss.hardcorealchemy.tweaks.network.MessageHearts;
import targoss.hardcorealchemy.tweaks.network.RequestCraftItemTimefrozen;
import targoss.hardcorealchemy.tweaks.research.Studies;

public class CommonProxy {
    public NetMessenger<HardcoreAlchemyTweaks> messenger;
    
    public void registerNetworking() {
        messenger = new NetMessenger<HardcoreAlchemyTweaks>(HardcoreAlchemyTweaks.MOD_ID.replace(HardcoreAlchemy.MOD_ID, HardcoreAlchemy.SHORT_MOD_ID))
            .register(new RequestCraftItemTimefrozen())
            .register(new MessageHearts());
    }
    
    public void preInit(FMLPreInitializationEvent event) {
        HardcoreAlchemy.proxy.addListener(new ListenerMobLevel());
        HardcoreAlchemy.proxy.addListener(new ListenerEntityVoidfade());
        HardcoreAlchemy.proxy.addListener(new ListenerBedBreakHarvest());
        HardcoreAlchemy.proxy.addListener(new ListenerInventoryFoodRot());
        HardcoreAlchemy.proxy.addListener(new ListenerWorldDifficulty());
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerSlip());
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerShield());
        HardcoreAlchemy.proxy.addListener(new ListenerMobEffect());
        HardcoreAlchemy.proxy.addListener(new ListenerCraftTimefrozen());
        HardcoreAlchemy.proxy.addListener(new ListenerHearts());
        HardcoreAlchemy.proxy.addListener(new ListenerHeartShards());

        // Initialize via classload
        new Items();
        HEARTS.register();
        // Initialize via classload
        new Studies();
        
        registerNetworking();
    }
    
    public void init(FMLInitializationEvent event) {
        Items.registerRecipes();
    }
}
