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

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.instinct.IInstinct;
import targoss.hardcorealchemy.instinct.Instincts;
import targoss.hardcorealchemy.instinct.Instincts.InstinctFactory;

public class CapabilityInstinct implements ICapabilityInstinct {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "instinct");
    
    private float instinct = ICapabilityInstinct.DEFAULT_INSTINCT_VALUE;
    private int inactiveInstinctTime = 0;
    private Map<ResourceLocation, InstinctEntry> instinctMap = new HashMap<>();
    private ResourceLocation activeInstinctId = null;

    @Override
    public float getInstinct() {
        return instinct;
    }

    @Override
    public void setInstinct(float instinct) {
        this.instinct = instinct;
    }

    @Override
    public void clearInstincts() {
        this.instinctMap.clear();
    }

    @Override
    public void addInstinct(InstinctFactory instinctFactory, EntityLivingBase morphEntity) {
        ICapabilityInstinct.InstinctEntry entry = new ICapabilityInstinct.InstinctEntry();
        entry.id = Instincts.REGISTRY.getKey(instinctFactory);
        entry.instinct = instinctFactory.createInstinct(morphEntity);
        entry.weight = entry.instinct.getWeight(morphEntity);
        this.instinctMap.put(entry.id, entry);
    }

    @Override
    public IInstinct getActiveInstinct() {
        if (activeInstinctId == null) {
            return null;
        }
        ICapabilityInstinct.InstinctEntry entry = instinctMap.get(activeInstinctId);
        if (entry == null) {
            activeInstinctId = null;
            return null;
        }
        return entry.instinct;
    }

    @Override
    public Map<ResourceLocation, InstinctEntry> getInstinctMap() {
        return instinctMap;
    }

    @Override
    public void setInstinctMap(Map<ResourceLocation, InstinctEntry> instinctMap) {
        this.instinctMap = instinctMap;
    }

    @Override
    public ResourceLocation getActiveInstinctId() {
        return activeInstinctId;
    }

    @Override
    public void setActiveInstinctId(ResourceLocation activeInstinctId) {
        this.activeInstinctId = activeInstinctId;
    }

    @Override
    public int getInactiveInstinctTime() {
        return this.inactiveInstinctTime;
    }

    @Override
    public void setInactiveInstinctTime(int inactiveInstinctTime) {
        this.inactiveInstinctTime = inactiveInstinctTime;
    }
}
