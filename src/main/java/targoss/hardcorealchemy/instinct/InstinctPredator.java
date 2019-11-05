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

package targoss.hardcorealchemy.instinct;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import targoss.hardcorealchemy.instinct.api.Instinct;
import targoss.hardcorealchemy.instinct.api.InstinctEffectDefinition;
import targoss.hardcorealchemy.instinct.api.InstinctNeedFactory;
import targoss.hardcorealchemy.util.EntityUtil;

public class InstinctPredator extends Instinct {
    @Override
    public boolean doesMorphEntityHaveInstinct(EntityLivingBase morphEntity) {
        return EntityUtil.getAiTargetTasks((EntityLiving)morphEntity).size() > 0;
    }

    @Override
    public List<InstinctNeedFactory> getNeeds(EntityLivingBase morphEntity) {
        return Lists.newArrayList(Instincts.NEED_ATTACK_PREY);
    }

    @Override
    public List<InstinctEffectDefinition> getEffects(EntityLivingBase morphEntity) {
        List<InstinctEffectDefinition> effects = new ArrayList<>();
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_HINDERED_MIND).setMaxInstinct(15.0F).setAmplifier(0.5F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_HINDERED_MIND).setMaxInstinct(10.0F).setAmplifier(1.0F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_HINDERED_MIND).setMaxInstinct(6.5F).setAmplifier(1.5F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_HINDERED_MIND).setMaxInstinct(5.0F).setAmplifier(1.75F));
        effects.add(new InstinctEffectDefinition(Instincts.EFFECT_HINDERED_MIND).setMaxInstinct(0.0F).setAmplifier(2.0F));
        return effects;
    }

}
