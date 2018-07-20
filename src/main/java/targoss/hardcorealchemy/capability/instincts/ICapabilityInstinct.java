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

package targoss.hardcorealchemy.capability.instincts;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.instinct.IInstinct;
import targoss.hardcorealchemy.instinct.Instincts.InstinctFactory;

public interface ICapabilityInstinct {
    public static final IAttribute MAX_INSTINCT = new RangedAttribute(null, HardcoreAlchemy.MOD_ID + ":max_instinct", 20.0D, Double.MIN_VALUE, Double.MAX_VALUE).setShouldWatch(true);
    public static final float DEFAULT_INSTINCT_VALUE = (float)(MAX_INSTINCT.getDefaultValue()*0.51);
    
    float getInstinct();
    
    void setInstinct(float instinct);
    
    public static class InstinctEntry {
        /** Used as the key in the instinct map */
        public ResourceLocation id;
        public IInstinct instinct;
        public float weight = 1.0F;
    }
    
    void clearInstincts();
    
    void addInstinct(InstinctFactory instinctFactory, EntityLivingBase morphEntity);
    
    @Nullable IInstinct getActiveInstinct();
    
    @Nonnull Map<ResourceLocation, InstinctEntry> getInstinctMap();
    
    void setInstinctMap(Map<ResourceLocation, InstinctEntry> instinctMap);
    
    ResourceLocation getActiveInstinctId();
    
    void setActiveInstinctId(ResourceLocation activeInstinctId);
    
    /**
     * Gets the time in ticks since an instinct has been inactive.
     * Used on server only.
     */
    int getInactiveInstinctTime();
    
    /**
     * Sets the time in ticks since an instinct has been inactive.
     * Used on server only.
     */
    void setInactiveInstinctTime(int inactiveInstinctTime);
}
