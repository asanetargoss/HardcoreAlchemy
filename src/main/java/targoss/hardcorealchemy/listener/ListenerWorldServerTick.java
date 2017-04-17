package targoss.hardcorealchemy.listener;

import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;

public class ListenerWorldServerTick {
    public static final int NETWORK_INTERVAL = 7; 
    private static int tick_count = 0;
    
    @SubscribeEvent
    public void onPostWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != Phase.END || event.side != Side.SERVER) {
            return;
        }
        tick_count++;
        if (tick_count > NETWORK_INTERVAL) {
            tick_count = 0;
            WorldServer worldServer = (WorldServer)event.world;
            //TODO: Get player list (convert to EntityPlayerMP) and send a packet of MessageHumanity to each client
            //thenetworkpackethandlerthing.INSTANCE.sendTo(...)
            
        }
    }
}
