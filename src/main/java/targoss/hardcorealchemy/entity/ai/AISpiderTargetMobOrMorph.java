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

package targoss.hardcorealchemy.entity.ai;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;

public class AISpiderTargetMobOrMorph<T extends EntityLivingBase> extends AIAttackTargetMobOrMorph<T> {

    public AISpiderTargetMobOrMorph(EntitySpider.AISpiderTarget<T> AIIgnoringMorph, EntityLiving entity) {
        super(AIIgnoringMorph, entity);
    }
    
    public boolean shouldExecute()
    {
        float f = this.taskOwner.getBrightness(1.0F);
        return f >= 0.5F ? false : super.shouldExecute();
    }
}
