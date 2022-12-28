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

// TODO: Need to specify the dimension *facepalm*
public interface ICapabilityWorldHumanity {
    public enum State {
        ACTIVE,
        REINCARNATED,
        DORMANT,
        DEACTIVATED
    }
    
    public static class Data {
        public Data(BlockPos pos, int dimension, State state) {
            this.pos = pos;
            this.dimension = dimension;
            this.state = state;
        }
        public BlockPos pos;
        public int dimension;
        public State state;
    };
    
    /** If a location is already registered for the given lifetimeUUID
     * or playerID, it may be cleared, but this is not guaranteed.
     * As long as player data is in a consistent state, there is a unique
     * (and possibly null) position for the given lifetimeUUID/playerID pair.
     * */
    void registerPhylactery(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension);
    State getPhylacteryState(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension);
    void setPhylacteryState(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension, State state);
    /** Returns true if there is a registered morph ability with the given player */
    boolean hasPlayerPhylactery(UUID lifetimeUUID, UUID playerUUID);
    /** Returns true if there was a registered morph ability with the given keys */
    boolean unregisterPhylactery(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension);
    /** Gets the phylactery associated with the player. The received keys may be
     * different than provided keys, indicating that an update is needed */
    public @Nullable Phylactery getPlayerPhylactery(UUID lifetimeUUID, UUID playerUUID);
    /** Gets the phylactery associated with the block position. The received keys may be
     * different than provided keys, indicating that an update is needed */
    public @Nullable Phylactery getBlockPhylactery(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension);
    
    public static class Phylactery {
        public Phylactery(UUID lifetimeUUID, UUID playerUUID, Data data) {
            this.lifetimeUUID = lifetimeUUID;
            this.playerUUID = playerUUID;
            this.data = data;
        }
        public UUID lifetimeUUID;
        public UUID playerUUID;
        public Data data;
    }
    Phylactery[] dumpPhylacteries();
    void clearAndPutPhylacteries(Phylactery[] phylacteries);
}
