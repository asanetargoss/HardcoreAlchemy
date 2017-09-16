package targoss.hardcorealchemy;

import net.minecraftforge.common.MinecraftForge;
import targoss.hardcorealchemy.listener.ListenerGuiHud;
import targoss.hardcorealchemy.listener.ListenerGuiInventory;

public class ClientProxy extends CommonProxy {
    @Override
    public void registerListeners() {
        super.registerListeners();
        // Technically, these only need to be registered on the logical client, but... meh
        MinecraftForge.EVENT_BUS.register(new ListenerGuiHud());
        MinecraftForge.EVENT_BUS.register(new ListenerGuiInventory());
    }
}
