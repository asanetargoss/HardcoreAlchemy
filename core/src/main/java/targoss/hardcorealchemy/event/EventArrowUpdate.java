package targoss.hardcorealchemy.event;

import net.minecraft.entity.projectile.EntityArrow;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class EventArrowUpdate extends Event {
    public final EntityArrow arrow;
    
    public EventArrowUpdate(EntityArrow arrow) {
        this.arrow = arrow;
    }
    
    public static boolean onArrowUpdate(EntityArrow arrow) {
        EventArrowUpdate event = new EventArrowUpdate(arrow);
        return MinecraftForge.EVENT_BUS.post(event);
    }
}
