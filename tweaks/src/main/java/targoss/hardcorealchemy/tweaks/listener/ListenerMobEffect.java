/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.listener;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.event.EventLivingAttack;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
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
