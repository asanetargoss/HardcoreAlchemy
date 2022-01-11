package targoss.hardcorealchemy.tweaks.listener;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.tweaks.event.EventLivingAttack;
import targoss.hardcorealchemy.tweaks.item.Items;
import targoss.hardcorealchemy.util.MobLists;

public class ListenerMobEffect extends HardcoreAlchemyListener {
    @SubscribeEvent
    public void onEntityAttackEnd(EventLivingAttack.End event) {
        if (!event.success) {
            // No damage dealt, etc
            return;
        }
        if (!(event.source instanceof EntityDamageSource)) {
            return;
        }
        Entity sourceEntity = ((EntityDamageSource)event.source).getEntity();
        if (sourceEntity == null) {
            return;
        }
        String sourceEntityString = EntityList.CLASS_TO_NAME.get(sourceEntity.getClass());
        if (!MobLists.SPIDER.equals(sourceEntityString)) {
            return;
        }
        if (event.entity.canBlockDamageSource(event.source)) {
            return;
        }
        // Normal spider applies slip potion effect for 10 seconds if not blocked by shield
        event.entity.addPotionEffect(new PotionEffect(Items.POTION_SLIP, 10 * 20));
    }
}
