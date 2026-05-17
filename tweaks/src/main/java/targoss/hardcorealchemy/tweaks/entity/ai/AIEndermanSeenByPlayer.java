/*
 * Copyright 2017-2026 asanetargoss
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

package targoss.hardcorealchemy.tweaks.entity.ai;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.tweaks.capability.hearts.ICapabilityHearts;
import targoss.hardcorealchemy.tweaks.item.Items;

public class AIEndermanSeenByPlayer extends EntityEnderman.AIFindPlayer {
    @CapabilityInject(ICapabilityHearts.class)
    public static final Capability<ICapabilityHearts> HEARTS_CAPABILITY = null;
    
    public EntityEnderman.AIFindPlayer delegate;

    public AIEndermanSeenByPlayer(EntityEnderman.AIFindPlayer delegate, EntityLiving entity) {
        super((EntityEnderman)entity);
        this.delegate = delegate;
    }
    
    @Override
    public boolean shouldExecute() {
        double targetingDistance = this.getTargetDistance();
        EntityPlayer player = this.delegate.enderman.world.getNearestAttackablePlayer(this.delegate.enderman.posX, this.delegate.enderman.posY, this.delegate.enderman.posZ, targetingDistance, targetingDistance, (Function<EntityPlayer, Double>)null, new Predicate<EntityPlayer>()
        {
            
            public boolean apply(@Nullable EntityPlayer player)
            {
                return player != null && AIEndermanSeenByPlayer.this.delegate.enderman.shouldAttackPlayer(player);
            }
        });
        if (player != null) {
            ICapabilityHearts hearts = player.getCapability(HEARTS_CAPABILITY, null);
            if (hearts != null && hearts.getSacrificed().contains(Items.HEART_VOID)) {
                return false;
            }
        }
        return this.delegate.shouldExecute();
    }
    
    @Override
    public boolean continueExecuting() {
        EntityPlayer player = this.delegate.player;
        if (player != null) {
            ICapabilityHearts hearts = player.getCapability(HEARTS_CAPABILITY, null);
            if (hearts != null && hearts.getSacrificed().contains(Items.HEART_VOID)) {
                return false;
            }
        }
        return delegate.continueExecuting();
    }
    
    @Override
    public void startExecuting() {
        delegate.startExecuting();
    }
    
    @Override
    public void resetTask() {
        delegate.resetTask();
    }
    
    @Override
    public void updateTask() {
        delegate.updateTask();
    }

}
