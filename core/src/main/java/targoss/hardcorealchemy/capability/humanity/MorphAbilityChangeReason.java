/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.capability.humanity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum MorphAbilityChangeReason {
    REGAINED_MORPH_ABILITY,
    NO_ABILITY,
    LOST_HUMANITY,
    FORGOT_LAST_FORM,
    FORGOT_HUMAN_FORM,
    REMEMBERED_HUMAN_FORM
    ;
    
    private static Map<String, MorphAbilityChangeReason> stringMap;
    
    static {
        stringMap = new HashMap<String, MorphAbilityChangeReason>();
        for (MorphAbilityChangeReason reason : EnumSet.allOf(MorphAbilityChangeReason.class)) {
            stringMap.put(reason.toString(), reason);
        }
    }
    
    public static MorphAbilityChangeReason fromString(String reasonString) {
        return stringMap.get(reasonString);
    }
}