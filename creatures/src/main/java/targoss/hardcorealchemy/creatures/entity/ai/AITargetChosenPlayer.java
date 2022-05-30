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

package targoss.hardcorealchemy.creatures.entity.ai;

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;
import targoss.hardcorealchemy.capability.entitystate.ICapabilityEntityState;
import targoss.hardcorealchemy.capability.entitystate.ProviderEntityState;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ProviderMisc;

public class AITargetChosenPlayer extends EntityAINearestAttackableTarget<EntityPlayer> {
    public static class ChosenPlayerPredicate implements com.google.common.base.Predicate<EntityPlayer> {
        public UUID cachedTargetPlayerUUID = null;
        
        public boolean apply(@Nullable EntityPlayer player) {
            if (cachedTargetPlayerUUID == null || player == null) {
                return false;
            }
            ICapabilityMisc misc = player.getCapability(ProviderMisc.MISC_CAPABILITY, null);
            return cachedTargetPlayerUUID.equals(misc.getLifetimeUUID());
        }
    }
    
    ChosenPlayerPredicate chosenPlayerPredicate = new ChosenPlayerPredicate();
    
    public AITargetChosenPlayer(EntityCreature creature) {
        super(creature, EntityPlayer.class, 10, true, false, null);
        this.targetEntitySelector = new Predicate<EntityPlayer>()
        {
            public boolean apply(@Nullable EntityPlayer player)
            {
                return player != null &&
                    chosenPlayerPredicate.apply(player) &&
                    EntitySelectors.NOT_SPECTATING.apply(player) &&
                    isSuitableTarget(player, false);
            }
        };
    }
    
    @Override
    public boolean shouldExecute() {
        ICapabilityEntityState entityState = taskOwner.getCapability(ProviderEntityState.CAPABILITY, null);
        if (entityState == null) {
            return false;
        }
        UUID cachedTargetPlayerUUID = entityState.getTargetPlayerID();
        chosenPlayerPredicate.cachedTargetPlayerUUID = cachedTargetPlayerUUID;
        if (cachedTargetPlayerUUID == null) {
            return false;
        }
        return super.shouldExecute();
    }
}
