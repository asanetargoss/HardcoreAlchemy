package targoss.hardcorealchemy.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class EventTakeStack extends Event {
    public final Slot slot;
    public final EntityPlayer player;
    
    public EventTakeStack(Slot slot, EntityPlayer player) {
        this.slot = slot;
        this.player = player;
    }
    
    @Cancelable
    public static class Pre extends EventTakeStack {
        private boolean canceled = false;
        
        public Pre(Slot slot, EntityPlayer player) {
            super(slot, player);
        }
        
        public boolean isCanceled() {
            return canceled;
        }
        
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
        
        public static boolean onTakeStackPre(Slot slot, EntityPlayer player) {
            Pre event = new Pre(slot, player);
            return !(MinecraftForge.EVENT_BUS.post(event));
        }
    }
}
