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

/**
 * When returned from Instinct.getEffects(), a list of effect definitions.
 * Otherwise, an internal class used by the instinct system.
 */
public class InstinctEffectWrapper {
    public InstinctEffect effect;
    public float amplifier = 0.0F;
    public float maxInstinct = 10.0F;
    
    public InstinctEffectWrapper () {}
    
    public InstinctEffectWrapper(InstinctEffect effect) {
        this.effect = effect;
    }
    
    public InstinctEffectWrapper(InstinctEffectWrapper wrapper) {
        this.effect = wrapper.effect;
        this.maxInstinct = wrapper.maxInstinct;
        this.amplifier = wrapper.amplifier;
    }
    
    public InstinctEffectWrapper setAmplifier(float amplifier) {
        this.amplifier = amplifier;
        return this;
    }
    
    public InstinctEffectWrapper setMaxInstinct(float maxInstinct) {
        this.maxInstinct = maxInstinct;
        return this;
    }
    
    public void combine(InstinctEffectWrapper wrapper) {
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
