package targoss.hardcorealchemy;

import net.minecraftforge.common.MinecraftForge;
import targoss.hardcorealchemy.listener.ListenerGui;

public class ClientProxy extends CommonProxy {
    @Override
    public void registerListeners() {
        super.registerListeners();
        // Technically, these only need to be registered on the logical client, but... meh
        MinecraftForge.EVENT_BUS.register(new ListenerGui());
    }
}
