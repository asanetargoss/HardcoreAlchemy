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
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.instinct.api.IInstinctEffectData;
import targoss.hardcorealchemy.instinct.api.Instinct;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.instinct.api.InstinctEffectDefinition;
import targoss.hardcorealchemy.instinct.api.InstinctNeedFactory;
import targoss.hardcorealchemy.instinct.internal.InstinctEffectWrapper;
import targoss.hardcorealchemy.instinct.internal.InstinctNeedWrapper;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.IDList;

public interface ICapabilityInstinct {
    public static final IAttribute MAX_INSTINCT = new RangedAttribute(null, HardcoreAlchemy.MOD_ID + ":max_instinct", 20.0D, Double.MIN_VALUE, Double.MAX_VALUE).setShouldWatch(true);
    public static final float DEFAULT_INSTINCT_VALUE = (float)(MAX_INSTINCT.getDefaultValue()*0.51);
    
    boolean getEnabled();
    
    void setEnabled(boolean enabled);
    
    /**
     * NOTE: This is updated each tick in InstinctSystem
     * and should be treated as read-only in other places.
     */
    float getInstinct();

    /**
     * NOTE: This is updated each tick in InstinctSystem
     * and should be treated as read-only in other places.
     */
    void setInstinct(float instinct);
    
    public static class InstinctEntry {
        public Instinct instinct;
        // Updated each tick by InstinctSystem
        public float instinctValue = DEFAULT_INSTINCT_VALUE;
        public List<InstinctNeedWrapper> needs;
        public List<InstinctEffectWrapper> effects;
        
        public InstinctEntry() {}
        public InstinctEntry(Instinct instinct) {
            this.instinct = instinct;
        }
        
        public List<InstinctNeedWrapper> getNeeds(@Nonnull EntityPlayer player) {
            if (needs == null) {
                List<InstinctNeedWrapper> needs = new ArrayList<>();
                for (InstinctNeedFactory needFactory : instinct.getNeeds(EntityUtil.getEffectiveEntity(player))) {
                    InstinctNeedWrapper needWrapper = new InstinctNeedWrapper();
                    needWrapper.factory = needFactory;
                    needs.add(needWrapper);
                }
                this.needs = needs;
            }
            return needs;
        }
        
        public List<InstinctEffectWrapper> getEffects(@Nonnull EntityPlayer player) {
            if (effects == null) {
                List<InstinctEffectDefinition> effectDefinitions = instinct.getEffects(EntityUtil.getEffectiveEntity(player));
                effects = new ArrayList<>(effectDefinitions.size());
                for (InstinctEffectDefinition definition : effectDefinitions) {
                    effects.add(new InstinctEffectWrapper(definition));
                }
            }
            return effects;
        }
        
        public void setNeeds(List<InstinctNeedWrapper> needs) {
            this.needs = needs;
        }
        
        public void setEffects(List<InstinctEffectWrapper> effects) {
            this.effects = effects;
        }
    }
    
    void addInstinct(Instinct instinct);
    
    /**
     * Clears the list of InstinctEntries
     */
    void clearInstincts(EntityPlayer player);
    
    List<InstinctEntry> getInstincts();
    
    void setInstincts(List<InstinctEntry> instincts);

    /**
     * Force an instinct effect to be active with the given amplitude.
     * Hold on to the returned ID and use it to clear the effect when
     * you no longer want it to be active.
     */
    int addForcedEffect(InstinctEffect effect, float amplitude);
    
    void removeForcedEffect(int effectForceKey);
    
    /**
     * NOTE: This map is re-created each tick in InstinctSystem
     * and should be treated as read-only in other places. To change instinct
     * state in a way that will persist, use clearInstincts/addInstinct, or getInstincts/setInstincts
     */
    Map<InstinctEffect, InstinctEffectWrapper> getActiveEffects();
    
    /**
     * NOTE: This map is re-created each tick in InstinctSystem
     * and should be treated as read-only in other places. To change instinct
     * state in a way that will persist, use clearInstincts/addInstinct, or getInstincts/setInstincts
     */
    void setActiveEffects(Map<InstinctEffect, InstinctEffectWrapper> effects);
    
    /**
     * Gets the time in ticks since an instinct need message has been shown.
     * Used on server only.
     */
    int getInstinctMessageTime();
    
    /**
     * Sets the time in ticks since an instinct need message has been shown.
     * Used on server only.
     */
    void setInstinctMessageTime(int inactiveInstinctTime);
    
    /**
     * Gets the data associated with an instinct effect, if any.
     * This can be used by effects to store state, or by needs
     * to modify effects.
     */
    IInstinctEffectData getInstinctEffectData(InstinctEffect instinctEffect);
    
    Map<InstinctEffect, IInstinctEffectData> getEffectData();
    
    void setEffectData(Map<InstinctEffect, IInstinctEffectData> effectData);
    
    Map<InstinctEffect, NBTTagCompound> getUninitializedEffectData();
    
    void setUninitializedEffectData(Map<InstinctEffect, NBTTagCompound> uninitializedEffectData);
    
    public static class ForcedEffectEntry {
        InstinctEffect effect;
        public float amplitude;
    }

    /**
     * Internal instinct function, do not use!
     * Use addForcedEffect, removeForcedEffect instead.
     * */
    IDList<ForcedEffectEntry> getForcedEffects();

    /**
     * Internal instinct function, do not use!
     * Use addForcedEffect, removeForcedEffect instead.
     * */
    void setForcedEffects(IDList<ForcedEffectEntry> forcedEffects);

}
