package targoss.hardcorealchemy.tweaks;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.tweaks.item.Items;
import targoss.hardcorealchemy.tweaks.listener.ListenerEntityVoidfade;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        HardcoreAlchemy.proxy.addListener(new ListenerEntityVoidfade.ClientSide());
        Items.ClientSide.registerSpecialModels();
    }
}
