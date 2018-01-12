package targoss.hardcorealchemy.listener;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.config.Configs;

/**
 * A listener contains Forge events and gameplay logic.
 * It is initialized with a Configs instance.
 */
public abstract class ConfiguredListener {
    private Configs configs;
    
    private ConfiguredListener() { }
    
    public ConfiguredListener(Configs configs) {
        this.configs = configs;
    }
    
    public void preInit(FMLPreInitializationEvent event) { }
    
    public void init(FMLInitializationEvent event) { }
    
    public void postInit(FMLPostInitializationEvent event) { }
    
    public void serverAboutToStart(FMLServerAboutToStartEvent event) { }
    
    public void serverStarting(FMLServerStartingEvent event) { }
    
    public void serverStarted(FMLServerStartedEvent event) { }
    
    public void serverStopping(FMLServerStoppingEvent event) { }
}