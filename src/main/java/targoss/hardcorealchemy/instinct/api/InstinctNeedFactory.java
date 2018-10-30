/*
 * Copyright 2018 asanetargoss
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

package targoss.hardcorealchemy.instinct.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class InstinctNeedFactory extends IForgeRegistryEntry.Impl<InstinctNeedFactory> {
    public final Class<? extends IInstinctNeed> instinctClass;
    public final IInstinctNeed instinctObject;
    
    public InstinctNeedFactory(Class<? extends IInstinctNeed> instinctClass) {
        this.instinctClass = instinctClass;
        this.instinctObject = createInstinct();
    }
    
    public IInstinctNeed createInstinct() {
        try {
            return instinctClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            HardcoreAlchemy.LOGGER.error("The instinct '" + Instincts.NEED_FACTORY_REGISTRY.getKey(this).toString() +
                    "' could not be created properly. Did the developer forget to define the default constructor?");
            e.printStackTrace();
        }
        return null;
    }
    
    public IInstinctNeed createNeed(EntityLivingBase morphEntity) {
        return instinctObject.createInstanceFromMorphEntity(morphEntity);
    }
}