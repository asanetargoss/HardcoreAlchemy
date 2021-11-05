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

package targoss.hardcorealchemy.capability.combatlevel;

import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class CapabilityCombatLevel implements ICapabilityCombatLevel {
    
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "combat_level");
    
    private int combatLevel;
    private boolean hasCombatLevel;
    
    public CapabilityCombatLevel() {
        this.combatLevel = 0;
        this.hasCombatLevel = false;
    }
    
    @Override
    public int getValue() {
        return combatLevel;
    }

    @Override
    public void setValue(int combatLevel) {
        this.combatLevel = Math.max(combatLevel, 0);
    }
    
    public static float getDamageMultiplier(int attackerLevel, int defenderLevel) {
        return Math.max(1.0F + (/*1.0F**/(
                ( ((float)attackerLevel+10.0F) / ((float)defenderLevel+10.0F) ) - 1.0F
                )), 1.0E-2F);
    }
    
    @Override
    public float getGivenDamageMultiplier(int otherCombatLevel) {
        return getDamageMultiplier(combatLevel, otherCombatLevel);
    }

    @Override
    public float getReceivedDamageMultiplier(int otherCombatLevel) {
        return getDamageMultiplier(otherCombatLevel, combatLevel);
    }

    @Override
    public boolean getHasCombatLevel() {
        return hasCombatLevel;
    }

    @Override
    public void setHasCombatLevel(boolean hasCombatLevel) {
        this.hasCombatLevel = hasCombatLevel;
    }

}
