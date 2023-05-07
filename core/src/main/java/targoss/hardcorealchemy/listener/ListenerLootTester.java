package targoss.hardcorealchemy.listener;

import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyCoreCoremod;

public class ListenerLootTester extends HardcoreAlchemyListener {
    public static String lastChatMessage = "";
    @SubscribeEvent
    public void debugChat(ServerChatEvent event) {
        assert(!HardcoreAlchemyCoreCoremod.obfuscated);
        
        lastChatMessage = event.getMessage();
        if (lastChatMessage == null) { lastChatMessage = ""; }
    }
}
