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

package targoss.hardcorealchemy.instinct.api;

/**
 * Returned from {@link targoss.hardcorealchemy.instinct.api.Instinct#getEffects Instinct.getEffects()}.
 * Defines an InstinctEffect to be activated for a given instinct.
 * 
 * Recommended maxInstinct values for InstinctEffects of different severity:
 * 
 * At or above 15.0:  No instinct effects active
 * 15.0 to 10.0: Effect amplifiers between 0.0 and 1.0. The player becomes aware their instinct
 *   needs are not being met.
 * 10.0 to 5.0: Effect amplifiers between 1.0 and 2.0. More effects become active or become more
 *   severe.
 * Below 5.0: Effects applied without prejudice.
 * 
 * See {@link targoss.hardcorealchemy.instinct.api.InstinctEffect InstinctEffect} for recommendations for
 * meanings of different effect amplifier levels. 
 */
public class InstinctEffectDefinition {
    public InstinctEffect effect;
    public float amplifier = 0.0F;
    public float maxInstinct = 10.0F;
    
    public InstinctEffectDefinition () {}
    
    public InstinctEffectDefinition(InstinctEffect effect) {
        this.effect = effect;
    }
    
    public InstinctEffectDefinition(InstinctEffectDefinition definition) {
        this.effect = definition.effect;
        this.maxInstinct = definition.maxInstinct;
        this.amplifier = definition.amplifier;
    }
    
    public InstinctEffectDefinition setAmplifier(float amplifier) {
        this.amplifier = amplifier;
        return this;
    }
    
    public InstinctEffectDefinition setMaxInstinct(float maxInstinct) {
        this.maxInstinct = maxInstinct;
        return this;
    }
    
    public void combine(InstinctEffectDefinition wrapper) {
        if (effect != wrapper.effect) {
            throw new IllegalArgumentException("Attempted to combine two InstinctEffectWrappers of different types");
        }
        maxInstinct = Math.min(maxInstinct, wrapper.maxInstinct);
        amplifier = Math.max(amplifier, wrapper.amplifier);
    }
    
    public void amplify(float amplifier) {
        this.amplifier = Math.max(this.amplifier, amplifier);
    }
}
