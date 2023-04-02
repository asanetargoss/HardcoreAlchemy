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

package targoss.hardcorealchemy.creatures.metamorph;

import mchorse.metamorph.api.MorphManager;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.creatures.metamorph.action.PrimitiveSustenance;
import targoss.hardcorealchemy.creatures.metamorph.action.UnderwaterFishing;

public class HcAMetamorphPack {
    public static void registerAbilities() {
        MorphManager manager = MorphManager.INSTANCE;
        
        manager.actions.put(HardcoreAlchemyCore.MOD_ID + ":underwater_fishing", new UnderwaterFishing());
        manager.actions.put(HardcoreAlchemyCore.MOD_ID + ":primitive_sustenance", new PrimitiveSustenance());
    }
}
