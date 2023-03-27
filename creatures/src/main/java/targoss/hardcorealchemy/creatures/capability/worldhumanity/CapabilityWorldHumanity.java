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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nullable;

import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

public class CapabilityWorldHumanity implements ICapabilityWorldHumanity {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "world_humanity");
    
    protected int entryCount = 0;
    protected static final int MAX_LOCATIONS_PER_PLAYER = 5;
    protected HashMap<UUID, ArrayList<Phylactery>> playerUUIDToPhylactery = new HashMap<>();

    // TODO: Oops! Should use the perma-UUID instead of getting the player ID, as the player ID can vary in dev and probably also when offline
    @Override
    public void registerPhylactery(Phylactery location) {
        ArrayList<Phylactery> phylacteries = playerUUIDToPhylactery.get(location.permanentUUID);
        if (phylacteries == null) {
            phylacteries = new ArrayList<>(1);
            playerUUIDToPhylactery.put(location.permanentUUID, phylacteries);
        }
        for (int i = 0; i < phylacteries.size(); ++i) {
            Phylactery phylactery = phylacteries.get(i);
            if (phylactery.lifetimeUUID.equals(location.lifetimeUUID) && phylactery.permanentUUID.equals(location.permanentUUID)) {
                // Already exists. Remove from list
                phylacteries.remove(i);
                --entryCount;
            }
        }
        if (phylacteries.size() > MAX_LOCATIONS_PER_PLAYER) {
            // Trim the array, discarding earlier entries as they are considered the oldest locations.
            for (int toRemove = phylacteries.size() - MAX_LOCATIONS_PER_PLAYER; toRemove > 0; --toRemove) {
                phylacteries.remove(0);
                --entryCount;
            }
        }
        if (entryCount == Integer.MAX_VALUE) {
            return;
        }
        phylacteries.add(location);
        ++entryCount;
    }

    @Override
    public void registerPhylactery(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension, AbstractMorph morphTarget) {
        registerPhylactery(new Phylactery(lifetimeUUID, playerUUID, pos, dimension, ICapabilityWorldHumanity.State.ACTIVE, morphTarget));
    }

    public @Nullable Phylactery getPhylacteryInternal(UUID lifetimeUUID, UUID playerUUID, @Nullable BlockPos pos, int dimension) {
        ArrayList<Phylactery> phylacteries = playerUUIDToPhylactery.get(playerUUID);
        if (phylacteries == null) {
            return null;
        }
        boolean foundMatchingPosition = false;
        
        int reincarnate_index = -1;
        for (int i = phylacteries.size()-1; i >= 0; --i) {
            Phylactery phylactery = phylacteries.get(i);
            // Should always be true: phylactery.playerUUID.equals(playerUUID))
            if (phylactery.lifetimeUUID.equals(lifetimeUUID)) {
                return phylactery;
            }
            if (pos == null) {
                if (phylactery.state == ICapabilityWorldHumanity.State.REINCARNATED) {
                    reincarnate_index = i;
                    break;
                }
            }
            else {
                if (phylactery.pos.equals(pos) && phylactery.dimension == dimension) {
                    foundMatchingPosition = true;
                    if (phylactery.state != ICapabilityWorldHumanity.State.REINCARNATED) {
                        return phylactery;
                    }
                    else {
                        reincarnate_index = i;
                        break;
                    }
                }
            }
        }
        
        if (reincarnate_index != -1) {
            if (pos == null) {
                for (int i = phylacteries.size() - 1; i > reincarnate_index; --i) {
                    Phylactery phylactery = phylacteries.get(i);
                    if (phylactery.state == ICapabilityWorldHumanity.State.ACTIVE) {
                        return phylactery;
                    }
                }
            }
            else {
                for (int i = reincarnate_index + 1; i < phylacteries.size(); ++i) {
                    Phylactery phylactery = phylacteries.get(i);
                    if (phylactery.state == ICapabilityWorldHumanity.State.ACTIVE &&
                            (phylactery.pos.equals(pos) && phylactery.dimension == dimension)) {
                        return phylactery;
                    }
                }
            }
        }
        
        if (pos != null && !foundMatchingPosition) {
            // One of several possibilities:
            // - Caller is trying to get the tile entity data at a position that does not have the data (likely a bug)
            // - Old phylacteries have been wiped for performance reasons (see registerPhylactery) (nothing we can do)
            // - Some sort of state bug
            HardcoreAlchemyCore.LOGGER.warn("Tile entity bookeeping not found for position: " + pos + ", dimension: " + dimension + ".");
        }
        return null;
    }
    
    @Override
    public @Nullable Phylactery getPlayerPhylactery(UUID lifetimeUUID, UUID playerUUID) {
        Phylactery phy = getPhylacteryInternal(lifetimeUUID, playerUUID, null, 0);
        assert(phy == null || phy.state == ICapabilityWorldHumanity.State.ACTIVE);
        return phy;
    }
    
    @Override
    public @Nullable Phylactery getBlockPhylactery(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension) {
        assert(pos != null);
        Phylactery phy = getPhylacteryInternal(lifetimeUUID, playerUUID, pos, dimension);
        assert(phy.state != ICapabilityWorldHumanity.State.REINCARNATED);
        return phy;
    }

    @Override
    public State getPhylacteryState(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension) {
        Phylactery phylactery = getPhylacteryInternal(lifetimeUUID, playerUUID, pos, dimension);
        if (phylactery == null) {
            return ICapabilityWorldHumanity.State.DEACTIVATED;
        }
        assert(phylactery.state != ICapabilityWorldHumanity.State.REINCARNATED);
        assert(phylactery.state != ICapabilityWorldHumanity.State.DEACTIVATED);
        return phylactery.state;
    }

    @Override
    public void setPhylacteryState(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension, State state) {
        Phylactery phylactery = getPhylacteryInternal(lifetimeUUID, playerUUID, pos, dimension);
        if (phylactery == null) {
            return;
        }
        phylactery.state = state;
    }

    @Override
    public boolean hasPlayerPhylactery(UUID lifetimeUUID, UUID playerUUID) {
        Phylactery phylactery = getPhylacteryInternal(lifetimeUUID, playerUUID, null, 0);
        return phylactery != null;
    }

    @Override
    public boolean hasBlockPhylactery(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension) {
        Phylactery phylactery = getPhylacteryInternal(lifetimeUUID, playerUUID, pos, dimension);
        return phylactery != null;
    }

    @Override
    public boolean unregisterPhylactery(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension) {
        ArrayList<Phylactery> phylacteries = playerUUIDToPhylactery.get(playerUUID);
        if (phylacteries == null) {
            return false;
        }
        boolean removed = false;
        for (int i = 0; i < phylacteries.size(); ++i) {
            Phylactery phy = phylacteries.get(i);
            if (phy.lifetimeUUID.equals(lifetimeUUID) && phy.permanentUUID.equals(playerUUID) &&
                    phy.pos.equals(pos) && phy.dimension == dimension) {
                phylacteries.remove(i);
                --entryCount;
                removed = true;
            }
            
        }
        return removed;
    }

    @Override
    public Collection<Phylactery> dumpPhylacteries() {
        ArrayList<Phylactery> locations = new ArrayList<>(entryCount);
        for (ArrayList<Phylactery> locs : playerUUIDToPhylactery.values()) {
            for (Phylactery loc : locs) {
                locations.add(loc);
            }
        }
        return locations;
    }

    @Override
    public void clearAndPutPhylacteries(Collection<Phylactery> phylacteries) {
        playerUUIDToPhylactery.clear();
        entryCount = 0;
        for (Phylactery phy : phylacteries) {
            registerPhylactery(phy);
        }
        for (ArrayList<Phylactery> phys : playerUUIDToPhylactery.values()) {
            phys.trimToSize();
        }
    }
}
