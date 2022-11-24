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

package targoss.hardcorealchemy.capability.worldhumanity;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.util.math.BlockPos;

public interface ICapabilityWorldHumanity {
    /** If a location is already registered for the given lifetimeUUID
     * or playerID, it may be cleared, but this is not guaranteed.
     * As long as player data is in a consistent state, there is a unique
     * (and possibly null) position for the given lifetimeUUID/playerID pair.
     * */
    void registerMorphAbilityLocation(UUID lifetimeUUID, UUID playerUUID, BlockPos pos);
    /** Returns true if there actually was a registered morph ability at the given location */
    boolean unregisterMorphAbilityLocation(UUID lifetimeUUID, UUID playerUUID, BlockPos pos);
    @Nullable BlockPos getMorphAbilityLocation(UUID lifetimeUUID, UUID playerUUID);
    
    public static class MorphAbilityLocation {
        public MorphAbilityLocation(UUID lifetimeUUID, UUID playerUUID, BlockPos pos) {
            this.lifetimeUUID = lifetimeUUID;
            this.playerUUID = playerUUID;
            this.pos = pos;
        }
        public UUID lifetimeUUID;
        public UUID playerUUID;
        public BlockPos pos;
    }
    MorphAbilityLocation[] dumpMorphAbilityLocations();
    void clearAndPutMorphAbilityLocations(MorphAbilityLocation[] morphAbilityLocations);
}
