package targoss.hardcorealchemy.tweaks.event;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import targoss.hardcorealchemy.coremod.CoremodHook;

public class EventMeleeAttack extends Event {
    public final EntityCreature attacker;
    public final EntityLivingBase target;
    
    public EventMeleeAttack(EntityCreature attacker, EntityLivingBase target) {
        this.attacker = attacker;
        this.target = target;
    }
    
    @CoremodHook
    public static void onMeleeAttack(EntityLivingBase target, EntityCreature attacker) {
        EventMeleeAttack event = new EventMeleeAttack(attacker, target);
        MinecraftForge.EVENT_BUS.post(event);
    }
}
