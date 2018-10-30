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
import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.instinct.api.Instinct;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.instinct.api.InstinctEffectWrapper;
import targoss.hardcorealchemy.instinct.api.InstinctNeedFactory;
import targoss.hardcorealchemy.instinct.api.InstinctNeedWrapper;
import targoss.hardcorealchemy.util.EntityUtil;

public interface ICapabilityInstinct {
    public static final IAttribute MAX_INSTINCT = new RangedAttribute(null, HardcoreAlchemy.MOD_ID + ":max_instinct", 20.0D, Double.MIN_VALUE, Double.MAX_VALUE).setShouldWatch(true);
    public static final float DEFAULT_INSTINCT_VALUE = (float)(MAX_INSTINCT.getDefaultValue()*0.51);
    
    float getInstinct();
    
    void setInstinct(float instinct);
    
    public static class InstinctEntry {
        public Instinct instinct;
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
                effects = instinct.getEffects(EntityUtil.getEffectiveEntity(player));
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
     * NOTE: This map is re-created each tick in ListenerPlayerInstinct
     * and should be treated as read-only in other places. To change instinct
     * state in a way that will persist, use clearInstincts/addInstinct, or getInstincts/setInstincts
     */
    Map<InstinctEffect, InstinctEffectWrapper> getActiveEffects();
    
    /**
     * NOTE: This map is re-created each tick in ListenerPlayerInstinct
     * and should be treated as read-only in other places. To change instinct
     * state in a way that will persist, use clearInstincts/addInstinct, or getInstincts/setInstincts
     */
    void setActiveEffects(Map<InstinctEffect, InstinctEffectWrapper> effects);
    
    @Deprecated
    public static class InstinctNeedEntry {
        /** Used as the key in the instinct map */
        public ResourceLocation id;
        public IInstinctNeed instinct;
        public float weight = 1.0F;
    }

    @Deprecated
    void clearInstinctNeeds();

    @Deprecated
    void addInstinctNeed(InstinctNeedFactory instinctFactory, EntityLivingBase morphEntity);

    @Deprecated
    @Nullable IInstinctNeed getActiveInstinct();

    @Deprecated
    @Nonnull Map<ResourceLocation, InstinctNeedEntry> getInstinctMap();

    @Deprecated
    void setInstinctMap(Map<ResourceLocation, InstinctNeedEntry> instinctMap);

    @Deprecated
    ResourceLocation getActiveInstinctId();

    @Deprecated
    void setActiveInstinctId(ResourceLocation activeInstinctId);
    
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
}
