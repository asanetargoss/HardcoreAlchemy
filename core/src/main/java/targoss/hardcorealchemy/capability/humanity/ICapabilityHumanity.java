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

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.text.ITextComponent;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

public interface ICapabilityHumanity {
    public static final IAttribute MAX_HUMANITY = new RangedAttribute(null, HardcoreAlchemyCore.MOD_ID + ":max_humanity", 4.0D, 1e-45, Double.MAX_VALUE).setShouldWatch(true);
    public static final double DEFAULT_HUMANITY_VALUE = MAX_HUMANITY.getDefaultValue();
    
    public abstract void setHumanity(double humanity);
    public abstract void setLastHumanity(double lastHumanity);
    public abstract void setMagicInhibition(double magicInhibition);
    public abstract void setHasLostHumanity(boolean hasLostHumanity);
    public abstract void setHasLostMorphAbility(boolean hasLostMorphAbility);
    public abstract void loseMorphAbilityFor(LostMorphReason reason);
    
    public abstract double getHumanity();
    public abstract double getLastHumanity();
    public abstract double getMagicInhibition();
    public abstract boolean getHasLostHumanity();
    public abstract boolean getHasLostMorphAbility();
    /** Returns false if the player is stuck as a morph */
    public abstract boolean isHuman();
    /** Returns false if the player is temporarily unable to morph */
    public abstract boolean canMorphRightNow();
    /** Returns false if the player is permanently unable to morph */
    public abstract boolean canMorph();
    /** Returns temporary or permanent reason the player cannot morph */
    public abstract ITextComponent explainWhyCantMorph();
    public abstract boolean shouldDisplayHumanity();
}
