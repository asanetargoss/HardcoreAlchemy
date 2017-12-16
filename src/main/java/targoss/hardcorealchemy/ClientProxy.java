package targoss.hardcorealchemy;

import net.minecraftforge.common.MinecraftForge;
import targoss.hardcorealchemy.listener.ListenerGuiHud;
import targoss.hardcorealchemy.listener.ListenerGuiInventory;

public class ClientProxy extends CommonProxy {
    @Override
    public void registerListeners() {
        super.registerListeners();
        
        MinecraftForge.EVENT_BUS.register(new ListenerGuiHud(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerGuiInventory(configs));
    }
}
