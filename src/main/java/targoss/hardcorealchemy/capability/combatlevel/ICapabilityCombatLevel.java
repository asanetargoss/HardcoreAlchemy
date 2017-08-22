package targoss.hardcorealchemy.capability.combatlevel;

/**
 * Combat level determines how much damage mobs give and receive with players
 * 
 * To be used by non-player monsters only
 */
public interface ICapabilityCombatLevel {
    public abstract int getValue();
    public abstract void setValue(int combatLevel);
    public abstract float getGivenDamageMultiplier(int othercombatLevel);
    public abstract float getReceivedDamageMultiplier(int othercombatLevel);
    /**
     * Determines whether this entity still needs its combat level to be set
     */
    public abstract boolean getHasCombatLevel();
    public abstract void setHasCombatLevel(boolean hasCombatLevel);
}
