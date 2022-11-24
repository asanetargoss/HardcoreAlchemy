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
    
    public double getHumanity();
    public double getMagicInhibition();
    public boolean getIsHumanFormInPhylactery();
    /** Returns false if the player is unable to morph using the
     *  metamorph morphing interface.
     *  Other methods of morphing may still be possible.
     *  If this is false, don't call MorphAPI.morph with force=true;
     *  call MorphState.forceForm instead. */
    public boolean canMorphRightNow();
    /** Returns false if the player is permanently or semi-permanently
     * unable to morph.
     * If false, the player can't morph, and may (or may not) be affected by various
     * long-term effects like instincts and nutrition.
     * See also shouldDisplayHumanity(), for other permamorph-related effects.
     * If this is false, don't call MorphAPI.morph with force=true;
     * call MorphState.forceForm instead. */
    public boolean canMorph();
    /** Returns the temporary or permanent reason the player cannot morph */
    public ITextComponent explainWhyCantMorph();
    /** Whether the humanity bar should display in HUD and update every tick.
     *  This is also used to determine if a player can acquire morphs or
     *  use high magic.
     *  Mechanics that affect the humanity bar (humanity, max humanity,
     *  magic inhibition) should not work if this is false. */
    public boolean shouldDisplayHumanity();
    
    /* Internal */
    public double getLastHumanity();
    public void setHumanity(double humanity);
    public void setLastHumanity(double lastHumanity);
    public void setMagicInhibition(double magicInhibition);
    public double getHumanityGainRate();
    public double getHumanityLossRate();
    /** Calculates threshold for displaying warnings when humanity gets critically low */
    public double getHumanityNMinutesLeft(int minutesLeft);
    
    /* These usually should not be called directly. Use MorphState.forceForm instead. */
    public void setHasForgottenHumanForm(boolean hasForgottenHumanForm);
    public void setHasLostHumanity(boolean hasLostHumanity);
    public void setHasForgottenMorphAbility(boolean hasForgottenMorphAbility);
    public void changeMorphAbilityFor(MorphAbilityChangeReason reason);
    
    /** Whether the player can morph into a human, assuming the player
        can still morph. */
    public boolean getHasForgottenHumanForm();
    public boolean getHasLostHumanity();
    public boolean getHasForgottenMorphAbility();
}
