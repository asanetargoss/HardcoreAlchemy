package targoss.hardcorealchemy;

import net.minecraftforge.common.MinecraftForge;
import targoss.hardcorealchemy.capability.combatlevel.CapabilityCombatLevel;
import targoss.hardcorealchemy.capability.humanity.CapabilityHumanity;
import targoss.hardcorealchemy.capability.killcount.CapabilityKillCount;
import targoss.hardcorealchemy.listener.ListenerPlayerHumanity;
import targoss.hardcorealchemy.listener.ListenerPlayerMorph;
import targoss.hardcorealchemy.listener.ListenerMobAI;
import targoss.hardcorealchemy.listener.ListenerMobLevel;
import targoss.hardcorealchemy.network.PacketHandler;

public class CommonProxy {
    
    public void registerListeners() {
        //TODO: reduce event listener code run on the client for ListenerPlayerMorph and ListenerHumanity
        MinecraftForge.EVENT_BUS.register(new ListenerPlayerMorph());
        MinecraftForge.EVENT_BUS.register(new ListenerPlayerHumanity());
        MinecraftForge.EVENT_BUS.register(new ListenerMobLevel());
    }
    
    public void registerCapabilities() {
        CapabilityKillCount.register();
        CapabilityHumanity.register();
        CapabilityCombatLevel.register();
    }
    
    public void registerNetworking() {
        PacketHandler.register();
    }
    
    public void postInit() {
        ListenerPlayerHumanity.postInit();
    }
}
