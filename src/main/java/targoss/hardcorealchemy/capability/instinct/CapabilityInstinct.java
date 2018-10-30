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

package targoss.hardcorealchemy.capability.instinct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.instinct.api.Instinct;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.instinct.api.InstinctEffectWrapper;
import targoss.hardcorealchemy.instinct.api.InstinctNeedFactory;
import targoss.hardcorealchemy.instinct.api.Instincts;

public class CapabilityInstinct implements ICapabilityInstinct {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "instinct");
    
    private float instinct = ICapabilityInstinct.DEFAULT_INSTINCT_VALUE;
    private List<ICapabilityInstinct.InstinctEntry> instincts = new ArrayList();
    private Map<InstinctEffect, InstinctEffectWrapper> activeEffects = new HashMap<>();
    
    private int instinctMessageTime = 0;
    @Deprecated
    private Map<ResourceLocation, InstinctNeedEntry> instinctMap = new HashMap<>();
    @Deprecated
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
    public void addInstinct(Instinct instinct) {
        instincts.add(new ICapabilityInstinct.InstinctEntry(instinct));
    }

    @Override
    public void clearInstincts(EntityPlayer player) {
        instincts.clear();
        for (InstinctEffectWrapper wrapper : activeEffects.values()) {
            wrapper.effect.onDeactivate(player, wrapper.amplifier);
        }
        activeEffects.clear();
    }

    @Override
    public List<InstinctEntry> getInstincts() {
        return instincts;
    }

    @Override
    public void setInstincts(List<InstinctEntry> instincts) {
        this.instincts = instincts;
    }

    @Override
    public Map<InstinctEffect, InstinctEffectWrapper> getActiveEffects() {
        return activeEffects;
    }

    @Override
    public void setActiveEffects(Map<InstinctEffect, InstinctEffectWrapper> effects) {
        this.activeEffects = effects;
    }

    @Override
    public void clearInstinctNeeds() {
        this.instinctMap.clear();
    }

    @Override
    public void addInstinctNeed(InstinctNeedFactory instinctFactory, EntityLivingBase morphEntity) {
        ICapabilityInstinct.InstinctNeedEntry entry = new ICapabilityInstinct.InstinctNeedEntry();
        entry.id = Instincts.NEED_FACTORY_REGISTRY.getKey(instinctFactory);
        entry.instinct = instinctFactory.createNeed(morphEntity);
        this.instinctMap.put(entry.id, entry);
    }

    @Override
    public IInstinctNeed getActiveInstinct() {
        if (activeInstinctId == null) {
            return null;
        }
        ICapabilityInstinct.InstinctNeedEntry entry = instinctMap.get(activeInstinctId);
        if (entry == null) {
            activeInstinctId = null;
            return null;
        }
        return entry.instinct;
    }

    @Override
    public Map<ResourceLocation, InstinctNeedEntry> getInstinctMap() {
        return instinctMap;
    }

    @Override
    public void setInstinctMap(Map<ResourceLocation, InstinctNeedEntry> instinctMap) {
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
    public int getInstinctMessageTime() {
        return this.instinctMessageTime;
    }

    @Override
    public void setInstinctMessageTime(int inactiveInstinctTime) {
        this.instinctMessageTime = inactiveInstinctTime;
    }
}
