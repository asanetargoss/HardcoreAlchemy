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

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.text.ITextComponent;
import targoss.hardcorealchemy.HardcoreAlchemy;

public interface ICapabilityHumanity {
    public static final IAttribute MAX_HUMANITY = new RangedAttribute(null, HardcoreAlchemy.MOD_ID + ":max_humanity", 4.0D, 1e-45, Double.MAX_VALUE).setShouldWatch(true);
    public static final double DEFAULT_HUMANITY_VALUE = MAX_HUMANITY.getDefaultValue();
    
    public abstract void setHumanity(double humanity);
    public abstract void setLastHumanity(double lastHumanity);
    public abstract void setHasLostHumanity(boolean hasLostHumanity);
    public abstract void setHasLostMorphAbility(boolean hasLostMorphAbility);
    public abstract void setIsMarried(boolean isMarried);
    public abstract void loseMorphAbilityFor(LostMorphReason reason);
    
    public abstract double getHumanity();
    public abstract double getLastHumanity();
    public abstract boolean getHasLostHumanity();
    public abstract boolean getHasLostMorphAbility();
    public abstract boolean getIsMarried();
    public abstract boolean canMorph();
    public abstract ITextComponent explainWhyCantMorph();
    public abstract boolean shouldDisplayHumanity();
}
