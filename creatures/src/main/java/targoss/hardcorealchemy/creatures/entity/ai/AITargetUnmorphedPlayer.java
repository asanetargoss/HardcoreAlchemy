/*
 * Copyright 2017-2018 asanetargoss
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

package targoss.hardcorealchemy.creatures.entity.ai;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.ai.EntityAITarget;
import net.minecraft.entity.player.EntityPlayer;
import targoss.hardcorealchemy.util.MorphExtension;

public class AITargetUnmorphedPlayer extends EntityAIFindEntityNearestPlayer {
    public EntityLiving entity;
    
    public AITargetUnmorphedPlayer(EntityAIFindEntityNearestPlayer ai, EntityLiving entity) {
        super(ai.entityLiving);
        this.entity = entity;
        this.predicate = new Predicate<Entity>()
        {
            public boolean apply(@Nullable Entity p_apply_1_)
            {
                if (!(p_apply_1_ instanceof EntityPlayer))
                {
                    return false;
                }
                else if (((EntityPlayer)p_apply_1_).capabilities.disableDamage)
                {
                    return false;
                }
                else
                {
                    double d0 = AITargetUnmorphedPlayer.this.maxTargetRange();

                    if (p_apply_1_.isSneaking())
                    {
                        d0 *= 0.800000011920929D;
                    }

                    if (p_apply_1_.isInvisible())
                    {
                        float f = ((EntityPlayer)p_apply_1_).getArmorVisibility();

                        if (f < 0.1F)
                        {
                            f = 0.1F;
                        }

                        d0 *= (double)(0.7F * f);
                    }

                    return (double)p_apply_1_.getDistanceToEntity(AITargetUnmorphedPlayer.this.entityLiving) > d0 ? false :
                        (EntityAITarget.isSuitableTarget(AITargetUnmorphedPlayer.this.entityLiving, (EntityLivingBase)p_apply_1_, false, true)
                        && MorphExtension.INSTANCE.isUnmorphed((EntityPlayer)p_apply_1_));
                }
            }
        };
    }
}
