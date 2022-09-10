/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.creatures.entity.Entities;
import targoss.hardcorealchemy.creatures.incantation.Incantations;
import targoss.hardcorealchemy.creatures.instinct.Instincts;
import targoss.hardcorealchemy.creatures.item.Items;
import targoss.hardcorealchemy.creatures.listener.ListenerBlockHeartOfForm;
import targoss.hardcorealchemy.creatures.listener.ListenerCapabilities;
import targoss.hardcorealchemy.creatures.listener.ListenerInstinctOverheat;
import targoss.hardcorealchemy.creatures.listener.ListenerMobAI;
import targoss.hardcorealchemy.creatures.listener.ListenerMorphExtension;
import targoss.hardcorealchemy.creatures.listener.ListenerNutritionExtension;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerHinderedMind;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerHumanity;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerInstinct;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerKillMastery;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerMorphState;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerMorphs;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerSealOfForm;
import targoss.hardcorealchemy.creatures.listener.ListenerSmallTweaks;
import targoss.hardcorealchemy.creatures.metamorph.HcAMetamorphPack;
import targoss.hardcorealchemy.creatures.network.MessageForceForm;
import targoss.hardcorealchemy.creatures.network.MessageHumanity;
import targoss.hardcorealchemy.creatures.network.MessageInstinct;
import targoss.hardcorealchemy.creatures.network.MessageInstinctEffects;
import targoss.hardcorealchemy.creatures.network.MessageInstinctNeedChanged;
import targoss.hardcorealchemy.creatures.network.MessageInstinctNeedState;
import targoss.hardcorealchemy.creatures.network.MessageKillCount;
import targoss.hardcorealchemy.creatures.network.MessageMaxHumanity;
import targoss.hardcorealchemy.creatures.network.MessageMorphState;
import targoss.hardcorealchemy.creatures.research.Studies;
import targoss.hardcorealchemy.network.NetMessenger;

public class CommonProxy {
    public NetMessenger<HardcoreAlchemyCreatures> messenger;
    
    public void registerNetworking() {
        messenger = new NetMessenger<HardcoreAlchemyCreatures>(HardcoreAlchemyCreatures.MOD_ID.replace(HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyCore.SHORT_MOD_ID))
            .register(new MessageHumanity())
            .register(new MessageInstinct())
            .register(new MessageInstinctNeedState())
            .register(new MessageInstinctEffects())
            .register(new MessageInstinctNeedChanged())
            .register(new MessageForceForm())
            .register(new MessageMorphState())
            .register(new MessageKillCount())
            .register(new MessageMaxHumanity());
    }
    
    public void preInit(FMLPreInitializationEvent event) {
        HardcoreAlchemyCore.proxy.addListener(new ListenerCapabilities());
        HardcoreAlchemyCore.proxy.addListener(new ListenerPlayerKillMastery());
        HardcoreAlchemyCore.proxy.addListener(new ListenerMorphExtension());
        HardcoreAlchemyCore.proxy.addListener(new ListenerNutritionExtension());
        HardcoreAlchemyCore.proxy.addListener(new ListenerPlayerMorphs());
        HardcoreAlchemyCore.proxy.addListener(new ListenerPlayerHumanity());
        HardcoreAlchemyCore.proxy.addListener(new ListenerMobAI());
        HardcoreAlchemyCore.proxy.addListener(new ListenerSmallTweaks());
        HardcoreAlchemyCore.proxy.addListener(new ListenerPlayerMorphState());
        HardcoreAlchemyCore.proxy.addListener(new ListenerPlayerInstinct());
        HardcoreAlchemyCore.proxy.addListener(new ListenerPlayerHinderedMind());
        HardcoreAlchemyCore.proxy.addListener(new ListenerInstinctOverheat());
        HardcoreAlchemyCore.proxy.addListener(new ListenerPlayerSealOfForm());
        HardcoreAlchemyCore.proxy.addListener(new ListenerBlockHeartOfForm());
        
        // Initialize via classload
        new Items();
        new Entities();
        new Incantations();
        new Studies();
        
        Instincts.INSTINCTS.register();
        Instincts.INSTINCT_NEED_FACTORIES.register();
        Instincts.INSTINCT_EFFECTS.register();
        
        registerNetworking();
    }

    public void init(FMLInitializationEvent event) {
        Items.registerRecipes();
        HcAMetamorphPack.registerAbilities();
    }
}
