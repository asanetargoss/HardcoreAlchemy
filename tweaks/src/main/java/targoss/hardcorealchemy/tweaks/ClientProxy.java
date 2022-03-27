package targoss.hardcorealchemy.tweaks;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.tweaks.item.Items;
import targoss.hardcorealchemy.tweaks.listener.ListenerClientItems;
import targoss.hardcorealchemy.tweaks.listener.ListenerCraftTimefrozen;
import targoss.hardcorealchemy.tweaks.listener.ListenerEntityVoidfade;
import targoss.hardcorealchemy.tweaks.listener.ListenerHearts;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        HardcoreAlchemy.proxy.addListener(new ListenerEntityVoidfade.ClientSide());
        HardcoreAlchemy.proxy.addListener(new ListenerClientItems());
        HardcoreAlchemy.proxy.addListener(new ListenerCraftTimefrozen.ClientSide());
        HardcoreAlchemy.proxy.addListener(new ListenerHearts.ClientSide());
        Items.ClientSide.registerSpecialModels();
    }
}
