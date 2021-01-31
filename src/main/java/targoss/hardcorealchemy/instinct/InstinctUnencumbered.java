/*
 * Copyright 2021 asanetargoss
 * 
 * This file is part of the Hardcore Alchemy capstone mod.
 * 
 * The Hardcore Alchemy capstone mod is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3 of the
 * License.
 * 
 * The Hardcore Alchemy capstone mod is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Hardcore Alchemy capstone mod. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.instinct;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import targoss.hardcorealchemy.instinct.api.Instinct;
import targoss.hardcorealchemy.instinct.api.InstinctEffectDefinition;
import targoss.hardcorealchemy.instinct.api.InstinctNeedFactory;
import targoss.hardcorealchemy.util.MobLists;

public class InstinctUnencumbered extends Instinct {
    protected static Set<String> humanoids = MobLists.getHumanoids();
    
    @Override
    public boolean doesMorphEntityHaveInstinct(EntityLivingBase morphEntity) {
        String entityName = EntityList.CLASS_TO_NAME.get(morphEntity.getClass());
        return !humanoids.contains(entityName);
    }

    @Override
    public List<InstinctNeedFactory> getNeeds(EntityLivingBase morphEntity) {
        return Lists.newArrayList(Instincts.NEED_UNENCUMBERED);
    }

    @Override
    public List<InstinctEffectDefinition> getEffects(EntityLivingBase morphEntity) {
        List<InstinctEffectDefinition> effects = new ArrayList<>();
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_VESTIPHOBIA).setMaxInstinct(15.0F).setAmplifier(0.5F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_VESTIPHOBIA).setMaxInstinct(10.0F).setAmplifier(1.0F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_VESTIPHOBIA).setMaxInstinct(0.0F).setAmplifier(2.0F));
        return effects;
    }

}
