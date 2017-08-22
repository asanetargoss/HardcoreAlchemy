package targoss.hardcorealchemy.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Like LivingAttackEvent, except you can modify the attack value, and cannot cancel it.
 * Useful when you want to modify the damage before armor is applied.
 */
//TODO: consider bringing a version of this event to Forge itself
public class EventLivingAttack extends Event {
    public final EntityLivingBase entity;
    public final DamageSource source;
    public float amount;
    
    public EventLivingAttack(EntityLivingBase entity, DamageSource source, float amount) {
        this.entity = entity;
        this.source = source;
        this.amount = amount;
    }
    
    public static float onLivingAttack(EntityLivingBase entity, DamageSource source, float amount) {
        EventLivingAttack event = new EventLivingAttack(entity, source, amount);
        return (MinecraftForge.EVENT_BUS.post(event) ? 0.0F : event.amount);
    }
}
