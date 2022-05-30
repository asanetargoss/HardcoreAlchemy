/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures.listener;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.creatures.item.Items;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;

/**
 * An event listener for miscellaneous changes that
 * don't fit anywhere in particular
 */
public class ListenerSmallTweaks extends HardcoreAlchemyListener {
    @CapabilityInject(ICapabilityMisc.class)
    private static final Capability<ICapabilityMisc> MISC_CAPABILITY = null;
    
    /**
     * The Obsidian Sheepman from Ad Inferos overrides
     * the Zombie class. This is all fine and dandy until
     * you realize that attacking sheepmen will cause zombies
     * to spawn, which doesn't make sense.
     */
    @SubscribeEvent
    @Optional.Method(modid=ModState.ADINFEROS_ID)
    public void onReinforceObsidianSheepman(ZombieEvent.SummonAidEvent event) {
        if (EntityList.getEntityString(event.getEntity()).equals(ModState.ADINFEROS_ID + ".ObsidianSheepman")) {
            event.setResult(Result.DENY);
        }
    }
    
    @SubscribeEvent
    public void onWaterMobUpdate(LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!entity.isPotionActive(Items.POTION_AIR_BREATHING)) {
            return;
        }
        if (entity.canBreatheUnderwater() ||
                ((entity instanceof EntityLiving) && ((EntityLiving)entity).getNavigator() instanceof PathNavigateSwimmer)
                ) {
            entity.setAir(300);
        }
    }
}
