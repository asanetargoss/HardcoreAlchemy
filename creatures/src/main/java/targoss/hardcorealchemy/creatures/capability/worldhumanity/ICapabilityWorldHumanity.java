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

package targoss.hardcorealchemy.creatures.capability.worldhumanity;

import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nullable;

import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.util.math.BlockPos;

public interface ICapabilityWorldHumanity {
    public enum State {
        ACTIVE,
        REINCARNATED,
        DORMANT,
        DEACTIVATED
    }
    
    /** If a location is already registered for the given lifetimeUUID
     * or playerID, it may be cleared, but this is not guaranteed.
     * As long as player data is in a consistent state, there is a unique
     * (and possibly null) position for the given lifetimeUUID/playerID pair.
     * */
    void registerPhylactery(Phylactery location);
    void registerPhylactery(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension, AbstractMorph morphTarget);
    State getPhylacteryState(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension);
    void setPhylacteryState(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension, State state);
    /** Returns true if there is a registered morph ability with the given player */
    boolean hasPlayerPhylactery(UUID lifetimeUUID, UUID playerUUID);
    /** Returns true if there is a registered morph ability with the given keys */
    boolean hasBlockPhylactery(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension);
    /** Returns true if there was a registered morph ability with the given keys */
    boolean unregisterPhylactery(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension);
    /** Gets the phylactery associated with the player. The received keys may be
     * different than provided keys, indicating that an update is needed */
    public @Nullable Phylactery getPlayerPhylactery(UUID lifetimeUUID, UUID playerUUID);
    /** Gets the phylactery associated with the block position. The received keys may be
     * different than provided keys, indicating that an update is needed */
    public @Nullable Phylactery getBlockPhylactery(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension);
    
    public static class Phylactery {
        public Phylactery(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension, ICapabilityWorldHumanity.State state, AbstractMorph morphTarget) {
            this.lifetimeUUID = lifetimeUUID;
            this.permanentUUID = playerUUID;
            this.pos = pos;
            this.dimension = dimension;
            this.state = state;
            this.morphTarget = morphTarget;
        }
        public UUID lifetimeUUID;
        public UUID permanentUUID;
        public BlockPos pos;
        public int dimension;
        public ICapabilityWorldHumanity.State state;
        public AbstractMorph morphTarget;
    }
    Collection<Phylactery> dumpPhylacteries();
    void clearAndPutPhylacteries(Collection<Phylactery> phylacteries);
}
