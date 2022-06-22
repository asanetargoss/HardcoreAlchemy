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

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

public class CapabilityHumanity implements ICapabilityHumanity {
    
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "humanity");
    
    private double humanity;
    // Humanity in the previous tick after humanity tick calculations; allows us to see if humanity was changed in other ways
    private double lastHumanity;
    private double magicInhibition;
    private boolean hasForgottenHumanForm;
    private boolean hasLostHumanity;
    private boolean hasLostMorphAbility;
    
    public CapabilityHumanity() {
        humanity = DEFAULT_HUMANITY_VALUE;
        lastHumanity = DEFAULT_HUMANITY_VALUE;
        hasLostHumanity = false;
        hasLostMorphAbility = false;
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
    public double getMagicInhibition() {
        return this.magicInhibition;
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
    public void loseMorphAbilityFor(LostMorphReason reason) {
        switch (reason) {
        case REGAINED_MORPH_ABILITY:
            this.hasLostHumanity = false;
            this.hasLostMorphAbility = false;
            break;
        case LOST_HUMANITY:
            this.hasLostHumanity = true;
            this.hasLostMorphAbility = false;
            break;
        case NO_ABILITY:
            this.hasLostMorphAbility = true;
            this.hasLostHumanity = false;
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
    public boolean isHuman() {
        return !(hasLostHumanity || hasLostMorphAbility);
    }
    
    @Override
    public boolean canMorphRightNow() {
        return !(hasLostHumanity || hasLostMorphAbility || magicInhibition >= humanity);
    }
    
    @Override
    public boolean canMorph() {
        return !(hasLostHumanity || hasLostMorphAbility);
    }
    
    @Override
    public boolean shouldDisplayHumanity() {
        return humanity > 0 && !(hasLostHumanity || hasLostMorphAbility);
    }
    
    @Override
    public ITextComponent explainWhyCantMorph() {
        if (hasLostMorphAbility) {
            return new TextComponentTranslation("hardcorealchemy.morph.disabled.noability");
        }
        if (hasLostHumanity) {
            return new TextComponentTranslation("hardcorealchemy.morph.disabled.nohumanity");
        }
        if (magicInhibition >= humanity) {
            return new TextComponentTranslation("hardcorealchemy.morph.disabled.magic_inhibition");
        }
        return new TextComponentString("");
    }

    @Override
    public void setHasLostMorphAbility(boolean hasLostMorphAbility) {
        this.hasLostMorphAbility = hasLostMorphAbility;
    }

    @Override
    public boolean getHasLostMorphAbility() {
        return hasLostMorphAbility;
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
