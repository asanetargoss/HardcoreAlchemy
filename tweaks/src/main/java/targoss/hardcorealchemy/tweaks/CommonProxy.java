package targoss.hardcorealchemy.tweaks;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.test.HardcoreAlchemyTests;
import targoss.hardcorealchemy.tweaks.item.Items;
import targoss.hardcorealchemy.tweaks.listener.ListenerBedBreakHarvest;
import targoss.hardcorealchemy.tweaks.listener.ListenerEntityVoidfade;
import targoss.hardcorealchemy.tweaks.listener.ListenerInventoryFoodRot;
import targoss.hardcorealchemy.tweaks.listener.ListenerMobLevel;
import targoss.hardcorealchemy.tweaks.listener.ListenerWorldDifficulty;
import targoss.hardcorealchemy.tweaks.test.suite.TestFoodRot;

public class CommonProxy {
    static {
        HardcoreAlchemyTests.TEST_SUITES.add(TestFoodRot.class);
    }
    
    public void preInit(FMLPreInitializationEvent event) {
        HardcoreAlchemy.proxy.addListener(new ListenerMobLevel());
        HardcoreAlchemy.proxy.addListener(new ListenerEntityVoidfade());
        HardcoreAlchemy.proxy.addListener(new ListenerBedBreakHarvest());
        HardcoreAlchemy.proxy.addListener(new ListenerInventoryFoodRot());
        HardcoreAlchemy.proxy.addListener(new ListenerWorldDifficulty());
    }
    
    public void init(FMLInitializationEvent event) {
        Items.registerRecipes();
    }
}
