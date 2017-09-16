package targoss.hardcorealchemy.capability.humanity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum LostMorphReason {
    /** When a player spawns without the ability to morph to begin with */
    NO_ABILITY,
    /** When a player spends too much time as a morph */
    LOST_HUMANITY,
    MARRIED,
    /** When a player uses high magic for the first time */
    MAGE
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