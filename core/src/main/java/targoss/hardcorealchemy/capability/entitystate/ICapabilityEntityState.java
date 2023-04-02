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

package targoss.hardcorealchemy.capability.entitystate;

import java.util.UUID;

import javax.annotation.Nullable;

/**
 * ICapabilityMisc but for entities.
 * Currently doesn't sync.
 * Used for AI and other miscellaneous entity things.
 */
public interface ICapabilityEntityState {
    /**
     * Note that this is NOT the same as the player's
     * game profile ID. It's ICapabilityMisc.getLifetimeUUID(),
     * which is unique to the player on each life.
     */
    UUID getTargetPlayerID();
    void setTargetPlayerID(@Nullable UUID playerID);

    /**
     * The age of the entity in ticks.
     */
    int getAge();
    void setAge(int age);
    /**
     * If set to a non-negative value, indicates the total time
     * the entity will exist.
     * If the entity's age reaches their lifetime, they will despawn.
     */
    int getLifetime();
    void setLifetime(int lifetime);
    
    boolean getTraveledDimensionally();
    void setTraveledDimensionally(boolean traveledDimensionally);
    /** Only valid if getTraveledDimensionally() returns true */
    int getPreviousDimension();
    void setPreviousDimension(int previousDimension);
}
