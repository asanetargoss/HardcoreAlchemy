/*
 * Copyright 2021 asanetargoss
 * 
 * This file is part of the Hardcore Alchemy capstone mod.
 * 
 * The Hardcore Alchemy capstone mod is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3 of the
 * License.
 * 
 * The Hardcore Alchemy capstone mod is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Hardcore Alchemy capstone mod. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.player.EntityPlayer;

public class AIPolarBearTargetMobOrMorph extends AIAttackTargetMobOrMorph<EntityPlayer> {

    public AIPolarBearTargetMobOrMorph(EntityPolarBear.AIAttackPlayer AIIgnoringMorph, EntityLiving entity) {
        super(AIIgnoringMorph, entity);
    }

    public boolean shouldExecute() {
        if (taskOwner.isChild()) {
            return false;
        }
        else {
            if (super.shouldExecute()) {
                for (EntityPolarBear entitypolarbear : taskOwner.world.getEntitiesWithinAABB(EntityPolarBear.class, taskOwner.getEntityBoundingBox().expand(8.0D, 4.0D, 8.0D))) {
                    if (entitypolarbear.isChild()) {
                        return true;
                    }
                }
            }

            taskOwner.setAttackTarget((EntityLivingBase)null);
            return false;
        }
    }

    protected double getTargetDistance() {
        return super.getTargetDistance() * 0.5D;
    }
}
