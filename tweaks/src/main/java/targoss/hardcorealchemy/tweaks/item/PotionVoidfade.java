/*
 * Copyright 2017-2025 asanetargoss
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

package targoss.hardcorealchemy.tweaks.item;

import static targoss.hardcorealchemy.item.Items.POTION_WATER_RESISTANCE;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.item.HcAPotion;
import targoss.hardcorealchemy.tweaks.capability.hearts.ICapabilityHearts;
import targoss.hardcorealchemy.tweaks.listener.ListenerHeartShards;
import targoss.hardcorealchemy.util.Color;
import targoss.hardcorealchemy.util.EntityExtension;

public class PotionVoidfade extends HcAPotion {
    
    @CapabilityInject(ICapabilityHearts.class)
    public static final Capability<ICapabilityHearts> HEARTS_CAPABILITY = null;

    public PotionVoidfade(boolean isBadEffect, Color color, int iconId, boolean halfPixelOffsetRight) {
        super(isBadEffect, color, iconId, halfPixelOffsetRight);
    }
    
    @Override
    public void performEffect(EntityLivingBase entity, int ampifier) {
        if (entity.isWet()) {
            // Extend duration
            int MIN_DURATION_WHEN_WET = (5 * 20) + 1;
            PotionEffect effect = entity.getActivePotionEffect(this);
            if (effect.getDuration() < MIN_DURATION_WHEN_WET) {
                PotionEffect extendedEffect = new PotionEffect(this, MIN_DURATION_WHEN_WET, effect.getAmplifier(), effect.getIsAmbient(), effect.doesShowParticles());
                entity.addPotionEffect(extendedEffect);
            }
            
            // Apply damage unless the player has the potion of water resistance
            if (!EntityExtension.INSTANCE.hasWaterAllergy(entity) && !entity.isPotionActive(POTION_WATER_RESISTANCE)) {
                entity.attackEntityFrom(DamageSource.drown, 1.0F);
                
                // Can acquire Heart of Void
                if (!entity.world.isRemote && (entity instanceof EntityPlayer)) {
                    EntityPlayer player = (EntityPlayer)entity;
                    ICapabilityHearts hearts = player.getCapability(HEARTS_CAPABILITY, null);
                    if (hearts != null) {
                        ListenerHeartShards.acquireHeartShard(player, hearts, Items.HEART_VOID);
                    }
                }
            }
        }
    }

}
