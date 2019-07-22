/*
 * Copyright 2019 asanetargoss
 * 
 * This file is part of Hardcore Alchemy.
 * 
 * Hardcore Alchemy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 * 
 * Hardcore Alchemy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hardcore Alchemy.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.entity.ai;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.player.EntityPlayer;
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
    
    public AITargetChosenPlayer(EntityCreature creature) {
        super(creature, EntityPlayer.class, 10, true, false, new ChosenPlayerPredicate());
    }
    
    @Override
    public boolean shouldExecute() {
        ICapabilityEntityState entityState = taskOwner.getCapability(ProviderEntityState.CAPABILITY, null);
        if (entityState == null) {
            return false;
        }
        UUID cachedTargetPlayerUUID = entityState.getTargetPlayerID();
        ((ChosenPlayerPredicate)targetEntitySelector).cachedTargetPlayerUUID = cachedTargetPlayerUUID;
        if (cachedTargetPlayerUUID == null) {
            return false;
        }
        return super.shouldExecute();
    }
}
