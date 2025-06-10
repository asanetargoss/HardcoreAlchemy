/*
 * Copyright 2017-2025 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.util;

import java.util.Set;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;

public class EntityExtension implements IEntityExtension {
    public static IEntityExtension INSTANCE = new EntityExtension();
    
    protected Set<String> ENTITIES_WATER_ALLERGY = MobLists.Type.WATER_ALLERGY.get();
    protected Set<String> ENTITIES_ENDER_WATER_ALLERGY = MobLists.Type.ENDER_WATER_ALLERGY.get();

    @Override
    public boolean hasWaterAllergy(EntityLivingBase entity) {
        return ENTITIES_WATER_ALLERGY.contains(EntityList.getEntityString(entity));
    }

    @Override
    public boolean hasEnderWaterAllergy(EntityLivingBase entity) {
        return ENTITIES_ENDER_WATER_ALLERGY.contains(EntityList.getEntityString(entity));
    }

}
