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

package targoss.hardcorealchemy.util;

import java.util.Arrays;
import java.util.List;

import net.minecraft.nbt.NBTBase;
import targoss.hardcorealchemy.ModStateException;

public class Serialization {
    private static final List<String> nbtTypeList = (List<String>)Arrays.asList(NBTBase.NBT_TYPES);
    private static int checkForNBTType(String nbtTypeString) {
        int nbtType = nbtTypeList.indexOf(nbtTypeString);
        if (NBT_COMPOUND_ID == -1) {
            throw new ModStateException(Serialization.class.getSimpleName() + ".NBT_" + nbtTypeString + "_ID could not be defined. Have NBT constants changed?");
        }
        return nbtType;
    }
    
    public static final int NBT_STRING_ID = checkForNBTType("STRING");
    public static final int NBT_COMPOUND_ID = checkForNBTType("COMPOUND");
}
