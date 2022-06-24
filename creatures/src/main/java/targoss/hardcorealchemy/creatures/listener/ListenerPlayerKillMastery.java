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

import java.util.HashSet;
import java.util.Set;

import mchorse.metamorph.api.events.AcquireMorphEvent;
import mchorse.metamorph.api.events.RegisterBlacklistEvent;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.EntityMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.creatures.capability.killcount.ICapabilityKillCount;
import targoss.hardcorealchemy.creatures.research.Studies;
import targoss.hardcorealchemy.event.EventLivingAttack;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.listener.ListenerPlayerResearch;
import targoss.hardcorealchemy.util.MorphExtension;

public class ListenerPlayerKillMastery extends HardcoreAlchemyListener {
    @CapabilityInject(ICapabilityKillCount.class)
    public static Capability<ICapabilityKillCount> KILL_COUNT_CAPABILITY = null;
    @CapabilityInject(ICapabilityHumanity.class)
    public static Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    
    /**
     * May hold a reference to the global morph blacklist. Don't modify this.
     */
    protected Set<String> damageReductionBlacklist = new HashSet<>();
    
    /**
     * Only reduce damage against creatures that the player can acquire a morph for.
     */
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onRegisterMorphBlacklist(RegisterBlacklistEvent event) {
        damageReductionBlacklist = event.blacklist;
    }
    
    public static void recalculateMasteredKills(EntityPlayer player) {
        IMorphing morphing = Morphing.get(player);
        if (morphing == null) {
            return;
        }
        ICapabilityKillCount killCount = player.getCapability(KILL_COUNT_CAPABILITY, null);
        if (killCount == null) {
            return;
        }
        for (AbstractMorph morph : morphing.getAcquiredMorphs()) {
            if (!(morph instanceof EntityMorph)) {
                continue;
            }
            killCount.addMasteredKill(morph.name);
        }
    }
    
    public static void addMasteredKill(AcquireMorphEvent.Post event) {
        AbstractMorph morph = event.morph;
        if (!(morph instanceof EntityMorph)) {
            return;
        }
        ICapabilityKillCount killCount = event.player.getCapability(KILL_COUNT_CAPABILITY, null);
        if (killCount == null) {
            return;
        }
        killCount.addMasteredKill(morph.name);
        ListenerPlayerResearch.acquireFactAndSendChatMessage(event.player, Studies.FACT_KILL_MASTERY_HINT);
    }
    
    protected float getUnmasteredKillDamageMultiplier(World world) {
        switch (world.getDifficulty()) {
        case PEACEFUL:
            return 0.95F;
        case EASY:
            return 0.9F;
        case NORMAL:
            return 0.75F;
        case HARD:
        default:
            return 0.5F;
        }
    }
    
    @SubscribeEvent
    public void onLivingHurt(EventLivingAttack.Start event) {
        DamageSource source = event.source;
        Entity entity = source.getEntity();
        if (entity == null) {
            // ¯\_(ツ)_/¯
            return;
        }
        World world = entity.world;
        if (world == null || world.isRemote) {
            return;
        }
        if (!(entity instanceof EntityLivingBase)) {
            return;
        }
        
        EntityLivingBase attacker = (EntityLivingBase)source.getEntity();
        if (!(attacker instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer attackerPlayer = (EntityPlayer)attacker;
        ICapabilityHumanity humanity = attackerPlayer.getCapability(HUMANITY_CAPABILITY, null);
        if (humanity == null) {
            return;
        }
        // Check what the player is attacking.
        // If the defender is a morphed player, treat the player like the entity they are currently morphed as.
        // If the entity is a morph the player can learn and the player doesn't have the morph yet, reduce damage dealth by the player.
        // Otherwise, keep damage the same.
        EntityLivingBase defenderEffectiveEntity = MorphExtension.INSTANCE.getEffectiveEntity(event.entity);
        if ((defenderEffectiveEntity instanceof EntityPlayer)) {
            if (!humanity.getHasForgottenHumanForm()) {
                return;
            }
        }
        else {
            String defenderEffectiveEntityName = EntityList.CLASS_TO_NAME.get(defenderEffectiveEntity.getClass());
            if (damageReductionBlacklist.contains(defenderEffectiveEntityName)) {
                return;
            }
            ICapabilityKillCount killCount = attackerPlayer.getCapability(KILL_COUNT_CAPABILITY, null);
            if (killCount == null) {
                return;
            }
            if (killCount.hasMasteredKill(defenderEffectiveEntityName)) {
                return;
            }
        }
        
        float hurtMultiplier = getUnmasteredKillDamageMultiplier(attackerPlayer.world);
        event.amount = event.amount * hurtMultiplier;
    }
}
