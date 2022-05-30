/*
 * Copyright 2017-2022 asanetargoss
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

package targoss.hardcorealchemy.creatures.instinct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import targoss.hardcorealchemy.creatures.instinct.api.Instinct;
import targoss.hardcorealchemy.creatures.instinct.api.InstinctEffectDefinition;
import targoss.hardcorealchemy.creatures.instinct.api.InstinctNeedFactory;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.MobLists;

public class InstinctHomesickNether extends Instinct {
    
    protected Set<String> validMobs = null;
    
    protected void initMobCache() {
        if (validMobs != null) {
            return;
        }
        validMobs = new HashSet<>();
        for (String mob : MobLists.getNetherMobs()) {
            if (!EntityUtil.isValidEntityName(mob)) {
                continue;
            }
            validMobs.add(mob);
        }
    }

    @Override
    public boolean doesMorphEntityHaveInstinct(EntityLivingBase morphEntity) {
        initMobCache();
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
        
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_NETHER_FEVER).setMaxInstinct(12.0F).setAmplifier(0.25F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_NETHER_FEVER).setMaxInstinct(10.0F).setAmplifier(0.5F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_NETHER_FEVER).setMaxInstinct(8.0F).setAmplifier(1.0F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_NETHER_FEVER).setMaxInstinct(7.0F).setAmplifier(1.25F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_NETHER_FEVER).setMaxInstinct(5.0F).setAmplifier(1.6F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_NETHER_FEVER).setMaxInstinct(3.0F).setAmplifier(2.0F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_NETHER_FEVER).setMaxInstinct(1.0F).setAmplifier(3.0F));
        
        return effects;
    }

}
