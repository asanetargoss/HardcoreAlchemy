/*
 * Copyright 2017-2023 asanetargoss
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
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyCoreCoremod;
import targoss.hardcorealchemy.creatures.instinct.api.Instinct;
import targoss.hardcorealchemy.creatures.instinct.api.InstinctEffectDefinition;
import targoss.hardcorealchemy.creatures.instinct.api.InstinctNeedFactory;
import targoss.hardcorealchemy.util.MobLists;

public class InstinctUnencumbered extends Instinct {
    protected static Set<String> humanoids = MobLists.getHumanoids();
    
    @Override
    public boolean doesMorphEntityHaveInstinct(EntityLivingBase morphEntity) {
        String entityName = EntityList.CLASS_TO_NAME.get(morphEntity.getClass());
        // TODO: Re-enable this outside of dev when more work is put into it
        return !humanoids.contains(entityName) && !HardcoreAlchemyCoreCoremod.obfuscated;
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
