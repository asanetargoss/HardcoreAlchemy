/**
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

package targoss.hardcorealchemy.entity.ai;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITargetNonTamed;
import net.minecraft.entity.passive.EntityTameable;

/**
 * Very similar to EntityAITargetNonTamed, but its superclass is changed so the mob respects morphs
 */
public class AIUntamedAttackMobOrMorph<T extends EntityLivingBase> extends AIAttackTargetMobOrMorph<T> {
    public final EntityTameable theTameable;

    public AIUntamedAttackMobOrMorph(EntityAITargetNonTamed<T> aiIgnoringMorph)
    {
        super(aiIgnoringMorph);
        this.theTameable = aiIgnoringMorph.theTameable;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        return !this.theTameable.isTamed() && super.shouldExecute();
    }
}
