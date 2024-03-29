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

package targoss.hardcorealchemy.creatures.instinct.internal;

import targoss.hardcorealchemy.creatures.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.creatures.instinct.api.InstinctEffectDefinition;

/** Internal to the instinct system. Use InstinctEffectDefinition instead. */
public class InstinctEffectWrapper extends InstinctEffectDefinition {
    public InstinctEffectWrapper () {}
    
    public InstinctEffectWrapper(InstinctEffect effect) {
        super(effect);
    }
    
    public InstinctEffectWrapper(InstinctEffectDefinition wrapper) {
        super(wrapper);
    }
}
