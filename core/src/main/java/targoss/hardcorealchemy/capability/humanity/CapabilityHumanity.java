/*
 * Copyright 2017-2023 asanetargoss
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

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

public class CapabilityHumanity implements ICapabilityHumanity {
    
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "humanity");
    
    // A freshly spawned player can only be in a morph for half a day
    protected static final double BASE_HUMANITY_LOSS_RATE = 2.0D/24000.0D*6.0D; // Per tick
    // Humanity is gained back 12x more slowly
    protected static final double BASE_HUMANITY_GAIN_RATE = BASE_HUMANITY_LOSS_RATE/12.0D;
    // Humanity is lost more slowly when morphed due to humanity phylactery
    protected static final double HUMANITY_LOSS_MULTIPLIER_FORGOTTEN_MORPH = 1 / 10.0;
    
    private double humanity;
    // Humanity in the previous tick after humanity tick calculations; allows us to see if humanity was changed in other ways
    private double lastHumanity;
    private double magicInhibition;
    private boolean isHumanFormInPhylactery;
    private boolean hasForgottenHumanForm;
    private boolean hasLostHumanity;
    private boolean hasForgottenMorphAbility;
    
    public CapabilityHumanity() {
        humanity = DEFAULT_HUMANITY_VALUE;
        lastHumanity = DEFAULT_HUMANITY_VALUE;
        hasLostHumanity = false;
        hasForgottenMorphAbility = false;
    }
    
    @Override
    public void setHumanity(double humanity) {
        this.humanity = humanity;
    }
    
    @Override
    public double getHumanity() {
        return this.humanity;
    }
    
    @Override
    public void setMagicInhibition(double magicInhibition) {
        this.magicInhibition = magicInhibition;
    }
    
    @Override
    public void setIsHumanFormInPhylactery(boolean isHumanFormInPhylactery) {
        this.isHumanFormInPhylactery = isHumanFormInPhylactery;
    }
    
    @Override
    public double getMagicInhibition() {
        return this.magicInhibition;
    }

    @Override
    public boolean getIsHumanFormInPhylactery() {
        return this.isHumanFormInPhylactery;
    }

    @Override
    public void setHasForgottenHumanForm(boolean hasForgottenHumanForm) {
        this.hasForgottenHumanForm = hasForgottenHumanForm;
    }

    @Override
    public boolean getHasForgottenHumanForm() {
        return hasForgottenHumanForm;
    }

    
    @Override
    public void setHasLostHumanity(boolean hasLostHumanity) {
        this.hasLostHumanity = hasLostHumanity;
    }
    
    @Override
    public void changeMorphAbilityFor(MorphAbilityChangeReason reason) {
        switch (reason) {
        case REGAINED_MORPH_ABILITY:
            hasLostHumanity = false;
            hasForgottenMorphAbility = false;
            hasForgottenHumanForm = false;
            break;
        case LOST_HUMANITY:
            hasLostHumanity = true;
            hasForgottenMorphAbility = false;
            humanity = 0.0F;
            break;
        case FORGOT_HUMAN_FORM:
            hasForgottenHumanForm = true;
            break;
        case CREATED_HUMAN_FORM_PHYLACTERY:
            hasForgottenHumanForm = true;
            isHumanFormInPhylactery = true;
            break;
        case DESTROYED_HUMAN_FORM_PHYLACTERY:
            isHumanFormInPhylactery = false;
            break;
        case REMEMBERED_HUMAN_FORM:
            hasForgottenHumanForm = false;
            break;
        case FORGOT_ABILITY:
            hasForgottenMorphAbility = true;
            break;
        case REMEMBERED_ABILITY:
            hasForgottenMorphAbility = false;
            break;
        default:
            break;
        }
    }
    
    @Override
    public boolean getHasLostHumanity() {
        return hasLostHumanity;
    }
    
    @Override
    public boolean canMorphRightNow() {
        return !(hasLostHumanity || hasForgottenMorphAbility || hasForgottenHumanForm || isHumanFormInPhylactery || magicInhibition >= humanity);
    }
    
    @Override
    public boolean canMorph() {
        return !(hasLostHumanity || hasForgottenMorphAbility || hasForgottenHumanForm || isHumanFormInPhylactery);
    }
    
    @Override
    public boolean shouldDisplayHumanity() {
        return humanity > 0 && !hasLostHumanity;
    }
    
    @Override
    public double getHumanityGainRate() {
        return BASE_HUMANITY_GAIN_RATE;
    }

    @Override
    public double getHumanityLossRate() {
        if (isHumanFormInPhylactery) {
            return BASE_HUMANITY_LOSS_RATE * HUMANITY_LOSS_MULTIPLIER_FORGOTTEN_MORPH;
        }
        return BASE_HUMANITY_LOSS_RATE;
    }

    @Override
    public double getHumanityNMinutesLeft(int minutesLeft) {
        return getHumanityLossRate() * 20.0D * 60.0D * minutesLeft;
    }

    @Override
    public ITextComponent explainWhyCantMorph() {
        if (hasLostHumanity) {
            return new TextComponentTranslation("hardcorealchemy.morph.disabled.nohumanity");
        }
        if (hasForgottenMorphAbility) {
            return new TextComponentTranslation("hardcorealchemy.morph.disabled.noability");
        }
        if (isHumanFormInPhylactery) {
            return new TextComponentTranslation("hardcorealchemy.morph.disabled.inphylactery");
        }
        if (hasForgottenHumanForm) {
            return new TextComponentTranslation("hardcorealchemy.morph.disabled.nohumanform");
        }
        if (magicInhibition >= humanity) {
            return new TextComponentTranslation("hardcorealchemy.morph.disabled.magic_inhibition");
        }
        return new TextComponentString("");
    }

    @Override
    public void setHasForgottenMorphAbility(boolean hasLostMorphAbility) {
        this.hasForgottenMorphAbility = hasLostMorphAbility;
    }

    @Override
    public boolean getHasForgottenMorphAbility() {
        return hasForgottenMorphAbility;
    }

    @Override
    public void setLastHumanity(double lastHumanity) {
        this.lastHumanity = lastHumanity;
    }

    @Override
    public double getLastHumanity() {
        return lastHumanity;
    }
}
