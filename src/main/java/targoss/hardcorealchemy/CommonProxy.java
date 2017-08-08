package targoss.hardcorealchemy;

import net.minecraftforge.common.MinecraftForge;
import targoss.hardcorealchemy.capabilities.CapabilityHumanity;
import targoss.hardcorealchemy.capabilities.CapabilityKillCount;
import targoss.hardcorealchemy.listener.ListenerHumanity;
import targoss.hardcorealchemy.listener.ListenerMorph;
import targoss.hardcorealchemy.network.PacketHandler;

public class CommonProxy {
    
    public void registerListeners() {
        MinecraftForge.EVENT_BUS.register(new ListenerMorph());
        MinecraftForge.EVENT_BUS.register(new ListenerHumanity());
    }
    
    public void registerCapabilities() {
        CapabilityKillCount.register();
        CapabilityHumanity.register();
    }
    
    public void registerNetworking() {
        PacketHandler.register();
    }
    
    public void postInit() {
        ListenerHumanity.postInit();
    }
}
