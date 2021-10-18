package targoss.hardcorealchemy.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

public class EventPlayerMorphStateChange extends Event {
    public final EntityPlayer player;
    
    protected EventPlayerMorphStateChange(EntityPlayer player) {
        this.player = player;
    }
    
    public static class Post extends EventPlayerMorphStateChange {
        public Post(EntityPlayer player) {
            super(player);
        }
    }
}
