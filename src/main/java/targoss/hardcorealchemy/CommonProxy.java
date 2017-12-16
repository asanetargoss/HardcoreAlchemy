package targoss.hardcorealchemy;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import targoss.hardcorealchemy.capability.combatlevel.CapabilityCombatLevel;
import targoss.hardcorealchemy.capability.food.CapabilityFood;
import targoss.hardcorealchemy.capability.humanity.CapabilityHumanity;
import targoss.hardcorealchemy.capability.killcount.CapabilityKillCount;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.listener.ListenerPlayerHumanity;
import targoss.hardcorealchemy.listener.ListenerPlayerMagic;
import targoss.hardcorealchemy.listener.ListenerPlayerMorph;
import targoss.hardcorealchemy.listener.ListenerBlock;
import targoss.hardcorealchemy.listener.ListenerCrops;
import targoss.hardcorealchemy.listener.ListenerInventoryFoodRot;
import targoss.hardcorealchemy.listener.ListenerMobAI;
import targoss.hardcorealchemy.listener.ListenerMobLevel;
import targoss.hardcorealchemy.listener.ListenerPacketUpdatePlayer;
import targoss.hardcorealchemy.listener.ListenerPlayerDiet;
import targoss.hardcorealchemy.network.PacketHandler;

public class CommonProxy {
    public Configs configs = new Configs();
    
    public void registerListeners() {
        MinecraftForge.EVENT_BUS.register(new ListenerPacketUpdatePlayer(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerPlayerMorph(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerPlayerHumanity(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerPlayerMagic(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerPlayerDiet(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerMobLevel(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerMobAI(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerBlock(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerInventoryFoodRot(configs));
        
        // Quick and dirty; subject to change
        MinecraftForge.EVENT_BUS.register(new ListenerCrops(configs));
    }
    
    public void registerCapabilities() {
        CapabilityKillCount.register();
        CapabilityHumanity.register();
        CapabilityCombatLevel.register();
        CapabilityFood.register();
    }
    
    public void registerNetworking() {
        PacketHandler.register();
    }
    
    public void postInit() {
        ListenerPlayerHumanity.postInit();
    }
}
