package targoss.hardcorealchemy.creatures;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.creatures.entity.Entities;
import targoss.hardcorealchemy.creatures.incantation.Incantations;
import targoss.hardcorealchemy.creatures.instinct.Instincts;
import targoss.hardcorealchemy.creatures.item.Items;
import targoss.hardcorealchemy.creatures.listener.ListenerCapabilities;
import targoss.hardcorealchemy.creatures.listener.ListenerInstinctOverheat;
import targoss.hardcorealchemy.creatures.listener.ListenerMobAI;
import targoss.hardcorealchemy.creatures.listener.ListenerMorphExtension;
import targoss.hardcorealchemy.creatures.listener.ListenerNutritionExtension;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerHinderedMind;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerHumanity;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerInstinct;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerMorphState;
import targoss.hardcorealchemy.creatures.listener.ListenerPlayerMorphs;
import targoss.hardcorealchemy.creatures.listener.ListenerSmallTweaks;
import targoss.hardcorealchemy.creatures.metamorph.HcAMetamorphPack;
import targoss.hardcorealchemy.creatures.network.MessageForceForm;
import targoss.hardcorealchemy.creatures.network.MessageInstinct;
import targoss.hardcorealchemy.creatures.network.MessageInstinctEffects;
import targoss.hardcorealchemy.creatures.network.MessageInstinctNeedChanged;
import targoss.hardcorealchemy.creatures.network.MessageInstinctNeedState;
import targoss.hardcorealchemy.network.NetMessenger;

public class CommonProxy {
    public NetMessenger<HardcoreAlchemyCreatures> messenger;
    
    public void registerNetworking() {
        messenger = new NetMessenger<HardcoreAlchemyCreatures>(HardcoreAlchemyCreatures.MOD_ID)
            .register(new MessageInstinct())
            .register(new MessageInstinctNeedState())
            .register(new MessageInstinctEffects())
            .register(new MessageInstinctNeedChanged())
            .register(new MessageForceForm());
    }
    
    public void preInit(FMLPreInitializationEvent event) {
        HardcoreAlchemy.proxy.addListener(new ListenerCapabilities());
        HardcoreAlchemy.proxy.addListener(new ListenerMorphExtension());
        HardcoreAlchemy.proxy.addListener(new ListenerNutritionExtension());
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerMorphs());
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerHumanity());
        HardcoreAlchemy.proxy.addListener(new ListenerMobAI());
        HardcoreAlchemy.proxy.addListener(new ListenerSmallTweaks());
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerMorphState());
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerInstinct());
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerHinderedMind());
        HardcoreAlchemy.proxy.addListener(new ListenerInstinctOverheat());
        
        // Initialize via classload
        new Items();
        new Entities();
        new Incantations();
        
        // asanetargoss @ 2021-10-03: Moved instinct registration from init to preInit
        // asanetargoss @ 2021-10-24: Moved instinct registration from core to creatures
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
