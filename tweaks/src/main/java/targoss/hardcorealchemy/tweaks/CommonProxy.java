package targoss.hardcorealchemy.tweaks;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.network.NetMessenger;
import targoss.hardcorealchemy.tweaks.item.Items;
import targoss.hardcorealchemy.tweaks.listener.ListenerBedBreakHarvest;
import targoss.hardcorealchemy.tweaks.listener.ListenerEntityVoidfade;
import targoss.hardcorealchemy.tweaks.listener.ListenerInventoryFoodRot;
import targoss.hardcorealchemy.tweaks.listener.ListenerMobEffect;
import targoss.hardcorealchemy.tweaks.listener.ListenerMobLevel;
import targoss.hardcorealchemy.tweaks.listener.ListenerPlayerShield;
import targoss.hardcorealchemy.tweaks.listener.ListenerPlayerSlip;
import targoss.hardcorealchemy.tweaks.listener.ListenerCraftTimefrozen;
import targoss.hardcorealchemy.tweaks.listener.ListenerWorldDifficulty;
import targoss.hardcorealchemy.tweaks.network.RequestCraftItemTimefrozen;
import targoss.hardcorealchemy.tweaks.research.Studies;

public class CommonProxy {
    public NetMessenger<HardcoreAlchemyTweaks> messenger;
    
    public void registerNetworking() {
        messenger = new NetMessenger<HardcoreAlchemyTweaks>(HardcoreAlchemyTweaks.MOD_ID.replace(HardcoreAlchemy.MOD_ID, HardcoreAlchemy.SHORT_MOD_ID))
            .register(new RequestCraftItemTimefrozen());
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
        
        // Initialize via classload
        new Studies();
        
        registerNetworking();
    }
    
    public void init(FMLInitializationEvent event) {
        Items.registerRecipes();
    }
}
