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

package targoss.hardcorealchemy.metamorph;

import mchorse.metamorph.api.MorphManager;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.metamorph.action.PrimitiveSustenance;
import targoss.hardcorealchemy.metamorph.action.UnderwaterFishing;

public class HcAMetamorphPack {
    public static void registerAbilities() {
        MorphManager manager = MorphManager.INSTANCE;
        
        manager.actions.put(HardcoreAlchemy.MOD_ID + ":underwater_fishing", new UnderwaterFishing());
        manager.actions.put(HardcoreAlchemy.MOD_ID + ":primitive_sustenance", new PrimitiveSustenance());
    }
}
