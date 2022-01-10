package targoss.hardcorealchemy.tweaks.event;

import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import targoss.hardcorealchemy.coremod.CoremodHook;

public class EventArrowHit extends Event {
    public final RayTraceResult rayTraceResult;
    public final DamageSource damageSource;
    
    public EventArrowHit(DamageSource damageSource, RayTraceResult rayTraceResult) {
        this.damageSource = damageSource;
        this.rayTraceResult = rayTraceResult;
    }
    
    @CoremodHook
    public static void onArrowHit(DamageSource damageSource, RayTraceResult rayTraceResult) {
        EventArrowHit event = new EventArrowHit(damageSource, rayTraceResult);
        MinecraftForge.EVENT_BUS.post(event);
    }
}
