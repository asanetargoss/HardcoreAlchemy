package targoss.hardcorealchemy.survival;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.survival.listener.ListenerCrops;
import targoss.hardcorealchemy.survival.listener.ListenerHarvestcraftRecipes;
import targoss.hardcorealchemy.survival.listener.ListenerMorphExtension;
import targoss.hardcorealchemy.survival.listener.ListenerNutritionExtension;
import targoss.hardcorealchemy.survival.listener.ListenerPlayerDiet;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        HardcoreAlchemy.proxy.addListener(new ListenerCrops());
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerDiet()); // 1.10-specific
        HardcoreAlchemy.proxy.addListener(new ListenerHarvestcraftRecipes());
        HardcoreAlchemy.proxy.addListener(new ListenerNutritionExtension());
        HardcoreAlchemy.proxy.addListener(new ListenerMorphExtension());
    }
}
