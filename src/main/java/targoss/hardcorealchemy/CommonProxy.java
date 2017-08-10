package targoss.hardcorealchemy;

import net.minecraftforge.common.MinecraftForge;
import targoss.hardcorealchemy.capability.humanity.CapabilityHumanity;
import targoss.hardcorealchemy.capability.killcount.CapabilityKillCount;
import targoss.hardcorealchemy.listener.ListenerPlayerHumanity;
import targoss.hardcorealchemy.listener.ListenerPlayerMorph;
import targoss.hardcorealchemy.network.PacketHandler;

public class CommonProxy {
    
    public void registerListeners() {
        MinecraftForge.EVENT_BUS.register(new ListenerPlayerMorph());
        MinecraftForge.EVENT_BUS.register(new ListenerPlayerHumanity());
    }
    
    public void registerCapabilities() {
        CapabilityKillCount.register();
        CapabilityHumanity.register();
    }
    
    public void registerNetworking() {
        PacketHandler.register();
    }
    
    public void postInit() {
        ListenerPlayerHumanity.postInit();
    }
}
