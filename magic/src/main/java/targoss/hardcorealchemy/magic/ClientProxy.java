package targoss.hardcorealchemy.magic;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.magic.listener.ListenerGuiInventory;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        
        HardcoreAlchemy.proxy.addListener(new ListenerGuiInventory());
    }
}
