package targoss.hardcorealchemy.capstone;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capstone.listener.ListenerGuides;
import targoss.hardcorealchemy.capstone.listener.ListenerInventoryExtension;
import targoss.hardcorealchemy.capstone.listener.ListenerPlayerInventory;
import targoss.hardcorealchemy.capstone.listener.ListenerPlayerMagicState;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerInventory());
        HardcoreAlchemy.proxy.addListener(new ListenerInventoryExtension());
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerMagicState());
        HardcoreAlchemy.proxy.addListener(new ListenerGuides());
    }
}
