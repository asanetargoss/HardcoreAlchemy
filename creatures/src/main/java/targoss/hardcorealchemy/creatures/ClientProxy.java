package targoss.hardcorealchemy.creatures;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.creatures.listener.ListenerGuiHud;
import targoss.hardcorealchemy.creatures.listener.ListenerRenderView;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        HardcoreAlchemy.proxy.addListener(new ListenerGuiHud());
        HardcoreAlchemy.proxy.addListener(new ListenerRenderView());
    }
}
