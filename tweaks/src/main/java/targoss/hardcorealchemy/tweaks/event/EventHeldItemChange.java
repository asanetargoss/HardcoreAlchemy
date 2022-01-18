package targoss.hardcorealchemy.tweaks.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import targoss.hardcorealchemy.coremod.CoremodHook;

public class EventHeldItemChange extends Event {
    public final CPacketHeldItemChange packet;
    public final EntityPlayer player;
    
    protected EventHeldItemChange(CPacketHeldItemChange packet, EntityPlayer player) {
        this.packet = packet;
        this.player = player;
    }
    
    public static class Pre extends EventHeldItemChange {
        public Pre(CPacketHeldItemChange packet, EntityPlayer player) {
            super(packet, player);
        }
    }
    
    public static class Post extends EventHeldItemChange {
        public Post(CPacketHeldItemChange packet, EntityPlayer player) {
            super(packet, player);
        }
    }
    
    @CoremodHook
    public static void ontHeldItemChangePre(CPacketHeldItemChange packet, EntityPlayer player) {
        EventHeldItemChange.Pre event = new EventHeldItemChange.Pre(packet, player);
        MinecraftForge.EVENT_BUS.post(event);
    }

    @CoremodHook
    public static void ontHeldItemChangePost(CPacketHeldItemChange packet, EntityPlayer player) {
        EventHeldItemChange.Post event = new EventHeldItemChange.Post(packet, player);
        MinecraftForge.EVENT_BUS.post(event);
    }
}
