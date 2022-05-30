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

package targoss.hardcorealchemy.capability.combatlevel;

/**
 * Combat level determines how much damage mobs give and receive with players
 * 
 * To reduce networking, this capability only exists on the server.
 * 
 * To be used by non-player monsters only (Players use Player.experienceLevel instead)
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
