package targoss.hardcorealchemy.survival;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.survival.listener.ListenerGuiHud;
import targoss.hardcorealchemy.survival.listener.ListenerGuiInventory;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        HardcoreAlchemy.proxy.addListener(new ListenerGuiHud());
        HardcoreAlchemy.proxy.addListener(new ListenerGuiInventory());
    }
}
