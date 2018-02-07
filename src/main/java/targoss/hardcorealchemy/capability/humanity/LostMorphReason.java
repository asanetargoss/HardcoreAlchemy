/*
 * Copyright 2017-2018 asanetargoss
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

package targoss.hardcorealchemy.capability.humanity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum LostMorphReason {
    /** When a player spawns without the ability to morph to begin with */
    NO_ABILITY,
    /** When a player spends too much time as a morph */
    LOST_HUMANITY,
    MARRIED
    ;
    
    private static Map<String, LostMorphReason> stringMap;
    
    static {
        stringMap = new HashMap<String, LostMorphReason>();
        for (LostMorphReason reason : EnumSet.allOf(LostMorphReason.class)) {
            stringMap.put(reason.toString(), reason);
        }
    }
    
    public static LostMorphReason fromString(String reasonString) {
        return stringMap.get(reasonString);
    }
}