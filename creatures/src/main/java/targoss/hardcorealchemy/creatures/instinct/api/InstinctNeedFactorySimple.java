/*
 * Copyright 2017-2023 asanetargoss
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

package targoss.hardcorealchemy.creatures.instinct.api;

import net.minecraft.entity.EntityLivingBase;

public class InstinctNeedFactorySimple extends InstinctNeedFactory {
    public final IInstinctNeed templateNeed;
    
    public InstinctNeedFactorySimple(IInstinctNeed templateNeed) {
        this.templateNeed = templateNeed;
    }
    
    @Override
    public IInstinctNeed createNeed(EntityLivingBase morphEntity) {
        return templateNeed.createInstanceFromMorphEntity(morphEntity);
    }
}