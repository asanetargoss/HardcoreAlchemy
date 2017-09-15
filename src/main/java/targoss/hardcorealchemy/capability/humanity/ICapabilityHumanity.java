package targoss.hardcorealchemy.capability.humanity;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.text.ITextComponent;
import targoss.hardcorealchemy.HardcoreAlchemy;

public interface ICapabilityHumanity {
    public static final IAttribute MAX_HUMANITY = new RangedAttribute(null, HardcoreAlchemy.MOD_ID + ":max_humanity", 4.0D, 1e-45, Double.MAX_VALUE).setShouldWatch(true);
    public static final double DEFAULT_HUMANITY_VALUE = MAX_HUMANITY.getDefaultValue();
    
    public abstract void setHumanity(double humanity);
    public abstract void setLastHumanity(double lastHumanity);
    public abstract void setTick(int tick);
    public abstract void setHasLostHumanity(boolean hasLostHumanity);
    public abstract void setHasLostMorphAbility(boolean hasLostMorphAbility);
    public abstract void setIsMarried(boolean isMarried);
    public abstract void setIsMage(boolean isMage);
    public abstract void setHighMagicOverride(boolean highMagicOverride);
    public abstract double getHumanity();
    public abstract double getLastHumanity();
    public abstract int getTick();
    public abstract boolean getHasLostHumanity();
    public abstract boolean getHasLostMorphAbility();
    public abstract boolean getIsMarried();
    public abstract boolean getIsMage();
    public abstract boolean getHighMagicOverride();
    public abstract boolean canMorph();
    public abstract boolean canUseHighMagic();
    public abstract ITextComponent explainWhyCantMorph();
    public abstract boolean shouldDisplayHumanity();
    
    /* A temporary variable used to prevent a player from seeing multiple notifications about
     * not being able to use magic in the same tick, due to multiple events being fired.
     */
    public abstract void setNotifiedMagicFail(boolean notifiedMagicFail);
    public abstract boolean getNotifiedMagicFail();
}
