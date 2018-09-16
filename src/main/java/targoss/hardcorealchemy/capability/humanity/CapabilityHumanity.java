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

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class CapabilityHumanity implements ICapabilityHumanity {
    
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "humanity");
    
    private double humanity;
    // Humanity in the previous tick after humanity tick calculations; allows us to see if humanity was changed in other ways
    private double lastHumanity;
    private boolean hasLostHumanity;
    private boolean hasLostMorphAbility;
    private boolean isMarried;
    
    public CapabilityHumanity() {
        humanity = DEFAULT_HUMANITY_VALUE;
        lastHumanity = DEFAULT_HUMANITY_VALUE;
        hasLostHumanity = false;
        hasLostMorphAbility = false;
        isMarried = false;
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
    public void setHasLostHumanity(boolean hasLostHumanity) {
        this.hasLostHumanity = hasLostHumanity;
    }
    
    @Override
    public void setIsMarried(boolean isMarried) {
        this.isMarried = isMarried;
    }
    
    @Override
    public void loseMorphAbilityFor(LostMorphReason reason) {
        switch (reason) {
        case LOST_HUMANITY:
            this.hasLostHumanity = true;
            this.isMarried = false;
            this.hasLostMorphAbility = false;
            break;
        case MARRIED:
            this.isMarried = true;
            this.hasLostHumanity = false;
            this.hasLostMorphAbility = false;
            break;
        case NO_ABILITY:
            this.hasLostMorphAbility = true;
            this.isMarried = false;
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
    public boolean getIsMarried() {
        return isMarried;
    }
    
    @Override
    public boolean canMorph() {
        return !(hasLostHumanity || hasLostMorphAbility || isMarried);
    }
    
    @Override
    public boolean shouldDisplayHumanity() {
        return humanity > 0 && !(hasLostHumanity || hasLostMorphAbility || isMarried);
    }
    
    @Override
    public ITextComponent explainWhyCantMorph() {
        if (hasLostMorphAbility) {
            return new TextComponentTranslation("hardcorealchemy.morph.disabled.noability");
        }
        if (hasLostHumanity) {
            return new TextComponentTranslation("hardcorealchemy.morph.disabled.nohumanity");
        }
        if (isMarried) {
            return new TextComponentTranslation("hardcorealchemy.morph.disabled.marriage");
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
