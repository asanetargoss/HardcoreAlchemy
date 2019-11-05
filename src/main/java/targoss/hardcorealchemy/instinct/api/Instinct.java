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

package targoss.hardcorealchemy.instinct.api;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

/**
 * An Instinct is applied to a player when they become stuck as a morph.
 * A player may be affected by one or more instincts, depending on
 * what creature they are stuck as.
 * 
 * Currently, only morphs based on EntityLivingBase are supported in this system.
 * 
 * The main thing visible to the player telling them the status of
 * their instincts is the instinct bar. The higher the instinct bar is,
 * the less bad things happen. Under ideal conditions, the instinct bar
 * increases over time.
 * 
 * A single instinct can be divided into two main components: needs and effects.
 * 
 * Needs ({@link targoss.hardcorealchemy.instinct.api.IInstinctNeed IInstinctNeed})
 * are responsible for keeping track of the player's
 * behavior, and determining if the behavior over time meets certain criteria.
 * The need can signal to the instinct state if it isn't being satisfied, and
 * in response, the instinct system will lower the instinct bar and
 * apply effects of varying severity.
 * Needs can also send messages to the player, queue custom update packets,
 * stop effects from being applied, and increase the amplitude of effects.
 * 
 * Effects ({@link targoss.hardcorealchemy.instinct.api.InstinctEffect InstinctEffect})
 * influence a player's stats and abilities.
 * When a need's instinct drops at or below a threshold,
 * the effect will usually become activated, and will not
 * deactivate until that need's instinct level rises above the threshold.
 * Effects have a default amplifier upon activation, but can also be given a
 * higher amplifier over time by other needs.
 */
public abstract class Instinct extends IForgeRegistryEntry.Impl<Instinct> {
    public Instinct() {}
    
    /**
     * Whether this instinct class should apply to players
     * permanently morphed as this entity.
     */
    public abstract boolean doesMorphEntityHaveInstinct(EntityLivingBase morphEntity);
    
    /**
     * Return a list of needs required to satisfy this instinct.
     * If any need is not fulfilled, effects may be applied.
     */
    public abstract List<InstinctNeedFactory> getNeeds(EntityLivingBase morphEntity);
    
    /**
     * Return a list of instinct effects to apply when the instinct
     * meter drops below a certain value.
     * Each InstinctEffect object itself is a singleton and should be registered to Forge.
     */
    public abstract List<InstinctEffectWrapper> getEffects(EntityLivingBase morphEntity);
}
