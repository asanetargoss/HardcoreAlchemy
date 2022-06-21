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

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ProviderMisc;
import targoss.hardcorealchemy.event.EventLivingAttack;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.tweaks.event.EventArrowHit;
import targoss.hardcorealchemy.tweaks.event.EventMeleeAttack;

/**
 * Listener which fixes fire and potion effects passing through shields.
 * 
 * A successful fix begins with one call to cacheDamageState, waits for damage to be applied or canceled,
 * and then calls restoreDamageState.
 */
public class ListenerPlayerShield extends HardcoreAlchemyListener {
    public void cacheDamageState(ICapabilityMisc misc, EntityLivingBase targetEntity, DamageSource source) {
        if (misc.getLastDamageSource() == source) {
            // Already cached
            // Avoid spoiling the existing cache
            return;
        }
        
        // See if the entity can block this damage source (ex: with shield), and if so,
        // begin blocking potion effects and fire as needed
        if (!targetEntity.canBlockDamageSource(source)) {
            // Don't need to handle this
            misc.setLastDamageSource(null);
            return;
        }
        misc.setLastDamageSource(source);
        
        {
            // Cache the current potion effects list
            // The original potions list will be restored after, effectively ignoring
            // any potion effect changes.
            Map<Potion, PotionEffect> potionsCache = misc.getActivePotionsCache();
            potionsCache.clear();
            for (PotionEffect effect : targetEntity.getActivePotionEffects()) {
                potionsCache.put(effect.getPotion(), effect);
            }
            // Reset fire state
            misc.setFireCache(targetEntity.fire);
        }
    }
    
    @SubscribeEvent
    public void onArrowHitEntity(EventArrowHit event) {
        if (!(event.rayTraceResult.entityHit instanceof EntityLivingBase)) {
            // Only interested in living entities
            return;
        }
        EntityLivingBase targetEntity = (EntityLivingBase)event.rayTraceResult.entityHit;
        // Only players are expected to have the misc capability
        if (!targetEntity.hasCapability(ProviderMisc.MISC_CAPABILITY, null)) {
            return;
        }
        ICapabilityMisc misc = targetEntity.getCapability(ProviderMisc.MISC_CAPABILITY, null);
        
        DamageSource source = event.damageSource;
        
        cacheDamageState(misc, targetEntity, source);
    }
    
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onEntityAttackStart(LivingAttackEvent event) {
        EntityLivingBase targetEntity = event.getEntityLiving();
        // Only players are expected to have the misc capability
        if (!targetEntity.hasCapability(ProviderMisc.MISC_CAPABILITY, null)) {
            return;
        }
        ICapabilityMisc misc = targetEntity.getCapability(ProviderMisc.MISC_CAPABILITY, null);

        DamageSource source = event.getSource();
        
        cacheDamageState(misc, targetEntity, source);
    }
    
    public void restoreDamageState(EntityLivingBase targetEntity) {
        if (!targetEntity.hasCapability(ProviderMisc.MISC_CAPABILITY, null)) {
            return;
        }
        ICapabilityMisc misc = targetEntity.getCapability(ProviderMisc.MISC_CAPABILITY, null);
        DamageSource lastDamageSource = misc.getLastDamageSource();
        if (misc.getLastDamageSource() == null) {
            return;
        }
        
        if (targetEntity.canBlockDamageSource(lastDamageSource)) {
            // Restore previous potion effect list
            // Need to call the add/remove method on all potion effects, in order to maintain state
            Map<Potion, PotionEffect> potionsCache = misc.getActivePotionsCache();
            Map<Potion, PotionEffect> potionsToRemove = new HashMap<>();
            for (PotionEffect effect : targetEntity.getActivePotionEffects()) {
                potionsToRemove.put(effect.getPotion(), effect);
            }
            for (Potion potion : potionsToRemove.keySet()) {
                targetEntity.removePotionEffect(potion);
            }
            for (PotionEffect effect : potionsCache.values()) {
                targetEntity.addPotionEffect(effect);
            }
            
            // Restore previous fire state
            targetEntity.fire = misc.getFireCache();
        }
    }
    
    @SubscribeEvent
    public void onEntityAttackEnd(EventLivingAttack.End event) {
        restoreDamageState(event.entity);
    }
    
    @SubscribeEvent
    public void onMeleeAttackEnd(EventMeleeAttack event) {
        restoreDamageState(event.target);
    }
}
