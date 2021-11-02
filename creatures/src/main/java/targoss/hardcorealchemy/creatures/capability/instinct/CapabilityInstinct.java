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

package targoss.hardcorealchemy.creatures.capability.instinct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.creatures.instinct.api.IInstinctEffectData;
import targoss.hardcorealchemy.creatures.instinct.api.Instinct;
import targoss.hardcorealchemy.creatures.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.creatures.instinct.internal.InstinctEffectWrapper;
import targoss.hardcorealchemy.util.IDList;

public class CapabilityInstinct implements ICapabilityInstinct {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "instinct");
    
    protected boolean enabled = true;
    private float instinct = ICapabilityInstinct.DEFAULT_INSTINCT_VALUE;
    private List<ICapabilityInstinct.InstinctEntry> instincts = new ArrayList<>();
    private Map<InstinctEffect, InstinctEffectWrapper> activeEffects = new HashMap<>();
    private Map<InstinctEffect, IInstinctEffectData> effectData = new HashMap<>();
    private Map<InstinctEffect, NBTTagCompound> uninitializedEffectData = new HashMap<>();
    int lastForcedEffectIndex = -1;
    private IDList<ForcedEffectEntry> forcedEffects = new IDList<>();
    
    private int instinctMessageTime = 0;
    
    @Override
    public boolean getEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

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
        effectData.clear();
        uninitializedEffectData.clear();
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
    public int getInstinctMessageTime() {
        return this.instinctMessageTime;
    }

    @Override
    public void setInstinctMessageTime(int inactiveInstinctTime) {
        this.instinctMessageTime = inactiveInstinctTime;
    }
    
    @Override
    public IInstinctEffectData getInstinctEffectData(InstinctEffect instinctEffect) {
        if (effectData.containsKey(instinctEffect)) {
            return effectData.get(instinctEffect);
        }
        
        IInstinctEffectData data = instinctEffect.createData();
        if (data != null) {
            effectData.put(instinctEffect, data);
            if (uninitializedEffectData.containsKey(instinctEffect)) {
                NBTTagCompound nbt = uninitializedEffectData.get(instinctEffect);
                uninitializedEffectData.remove(instinctEffect);
                data.deserializeNBT(nbt);
            }
            return data;
        }
        
        return null;
    }

    @Override
    public Map<InstinctEffect, IInstinctEffectData> getEffectData() {
        return effectData;
    }

    @Override
    public void setEffectData(Map<InstinctEffect, IInstinctEffectData> effectData) {
        this.effectData = effectData;
    }

    @Override
    public Map<InstinctEffect, NBTTagCompound> getUninitializedEffectData() {
        return uninitializedEffectData;
    }

    @Override
    public void setUninitializedEffectData(Map<InstinctEffect, NBTTagCompound> uninitializedEffectData) {
        this.uninitializedEffectData = uninitializedEffectData;
    }

    @Override
    public int addForcedEffect(InstinctEffect effect, float amplitude) {
        ForcedEffectEntry entry = new ForcedEffectEntry();
        entry.effect = effect;
        entry.amplitude = amplitude;
        return forcedEffects.add(entry);
    }

    @Override
    public void removeForcedEffect(int effectForceKey, InstinctEffect expectedEffect) {
        try {
            ForcedEffectEntry entry = forcedEffects.get(effectForceKey);
            if (!expectedEffect.equals(entry.effect)) {
                HardcoreAlchemy.LOGGER.warn("A forced instinct effect of type " +
                        expectedEffect.getRegistryName() + " could not be removed at index " +
                        effectForceKey + ".");
            }
            else {
                forcedEffects.remove(effectForceKey);
            }
        }
        catch (Exception e) {
            // This really shouldn't happen, but if it does...
            assert(false);
            HardcoreAlchemy.LOGGER.error(
                    "An error occurred removing an instinct effect at index  " + effectForceKey + ". " +
                    "This may be a sign of a severe problem with handling instinct state.",
                    e);
            int n = forcedEffects.getInternalList().size();
            for (int i = 0; i < n; ++i) {
                ForcedEffectEntry entry = forcedEffects.get(i);
                if (entry == null) {
                    continue;
                }
                if (expectedEffect.equals(entry.effect)) {
                    forcedEffects.remove(i);
                    break;
                }
            }
        }
    }

    @Override
    public IDList<ForcedEffectEntry> getForcedEffects() {
        return forcedEffects;
    }

    @Override
    public void setForcedEffects(IDList<ForcedEffectEntry> forcedEffects) {
        this.forcedEffects = forcedEffects;
    }
}
