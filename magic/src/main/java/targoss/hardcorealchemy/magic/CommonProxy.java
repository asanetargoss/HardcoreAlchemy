package targoss.hardcorealchemy.magic;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.magic.listener.ListenerInventoryExtension;
import targoss.hardcorealchemy.magic.listener.ListenerPlayerMagic;
import targoss.hardcorealchemy.magic.listener.ListenerPlayerMagicState;

public class CommonProxy {
    
    public void preInit(FMLPreInitializationEvent event) {
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerMagic());
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerMagicState());
        HardcoreAlchemy.proxy.addListener(new ListenerInventoryExtension());
    }
}
