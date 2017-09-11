package targoss.hardcorealchemy;

import net.minecraftforge.common.MinecraftForge;
import targoss.hardcorealchemy.capability.combatlevel.CapabilityCombatLevel;
import targoss.hardcorealchemy.capability.food.CapabilityFood;
import targoss.hardcorealchemy.capability.humanity.CapabilityHumanity;
import targoss.hardcorealchemy.capability.killcount.CapabilityKillCount;
import targoss.hardcorealchemy.listener.ListenerPlayerHumanity;
import targoss.hardcorealchemy.listener.ListenerPlayerMagic;
import targoss.hardcorealchemy.listener.ListenerPlayerMorph;
import targoss.hardcorealchemy.listener.ListenerMobAI;
import targoss.hardcorealchemy.listener.ListenerMobLevel;
import targoss.hardcorealchemy.listener.ListenerPacketUpdatePlayer;
import targoss.hardcorealchemy.listener.ListenerPlayerDiet;
import targoss.hardcorealchemy.network.PacketHandler;

public class CommonProxy {
    
    public void registerListeners() {
        MinecraftForge.EVENT_BUS.register(new ListenerPacketUpdatePlayer());
        MinecraftForge.EVENT_BUS.register(new ListenerPlayerMorph());
        MinecraftForge.EVENT_BUS.register(new ListenerPlayerHumanity());
        MinecraftForge.EVENT_BUS.register(new ListenerPlayerMagic());
        MinecraftForge.EVENT_BUS.register(new ListenerPlayerDiet());
        MinecraftForge.EVENT_BUS.register(new ListenerMobLevel());
        MinecraftForge.EVENT_BUS.register(new ListenerMobAI());
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
