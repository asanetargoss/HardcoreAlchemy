/*
 * Copyright 2019 asanetargoss
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

package targoss.hardcorealchemy.instinct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import targoss.hardcorealchemy.instinct.api.Instinct;
import targoss.hardcorealchemy.instinct.api.InstinctEffectDefinition;
import targoss.hardcorealchemy.instinct.api.InstinctNeedFactory;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.MobLists;

public class InstinctHomesickNature extends Instinct {
    
    protected static Set<String> validMobs = new HashSet<>();
    static {
        for (String mob : MobLists.getLandAnimals()) {
            if (!EntityUtil.isValidEntityName(mob)) {
                continue;
            }
            validMobs.add(mob);
        }
    }

    @Override
    public boolean doesMorphEntityHaveInstinct(EntityLivingBase morphEntity) {
        return validMobs.contains(EntityList.getEntityString(morphEntity));
    }

    @Override
    public List<InstinctNeedFactory> getNeeds(EntityLivingBase morphEntity) {
        List<InstinctNeedFactory> needs = new ArrayList<>();
        
        needs.add(Instincts.NEED_SPAWN_ENVIRONMENT);
        
        return needs;
    }

    @Override
    public List<InstinctEffectDefinition> getEffects(EntityLivingBase morphEntity) {
        List<InstinctEffectDefinition> effects = new ArrayList<>();
        
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_HUNTED).setMaxInstinct(15.0F).setAmplifier(0.5F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_HUNTED).setMaxInstinct(10.0F).setAmplifier(1.0F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_HUNTED).setMaxInstinct(7.5F).setAmplifier(1.5F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_HUNTED).setMaxInstinct(5.0F).setAmplifier(2.0F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_HUNTED).setMaxInstinct(4.0F).setAmplifier(3.0F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_HUNTED).setMaxInstinct(2.0F).setAmplifier(4.0F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_HUNTED).setMaxInstinct(1.0F).setAmplifier(5.0F));
        
        return effects;
    }

}
