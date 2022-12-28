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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

public class CapabilityWorldHumanity implements ICapabilityWorldHumanity {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "world_humanity");
    
    protected int entryCount = 0;
    protected static final int MAX_LOCATIONS_PER_PLAYER = 5;
    protected HashMap<UUID, ArrayList<Phylactery>> playerUUIDToPhylactery = new HashMap<>();

    protected void registerPhylactery(Phylactery location) {
        ArrayList<Phylactery> phylacteries = playerUUIDToPhylactery.get(location.playerUUID);
        if (phylacteries == null) {
            phylacteries = new ArrayList<>(1);
            playerUUIDToPhylactery.put(location.playerUUID, phylacteries);
        }
        for (int i = 0; i < phylacteries.size(); ++i) {
            Phylactery phylactery = phylacteries.get(i);
            if (phylactery.lifetimeUUID.equals(location.lifetimeUUID) && phylactery.playerUUID.equals(location.playerUUID)) {
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
    public void registerPhylactery(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension) {
        registerPhylactery(new Phylactery(lifetimeUUID, playerUUID, new Data(pos, dimension, ICapabilityWorldHumanity.State.ACTIVE)));
    }

    public @Nullable Phylactery getPhylacteryInternal(UUID lifetimeUUID, UUID playerUUID, @Nullable BlockPos pos, int dimension) {
        ArrayList<Phylactery> phylacteries = playerUUIDToPhylactery.get(playerUUID);
        if (phylacteries == null) {
            return null;
        }
        
        int reincarnate_index = -1;
        for (int i = 0; i < phylacteries.size(); ++i) {
            Phylactery phylactery = phylacteries.get(i);
            // Should always be true: phylactery.playerUUID.equals(playerUUID))
            if (phylactery.lifetimeUUID.equals(lifetimeUUID)) {
                return phylactery;
            }
            if (reincarnate_index == -1) {
                if (phylactery.data.state == ICapabilityWorldHumanity.State.REINCARNATED &&
                        (pos == null || (phylactery.data.pos.equals(pos) && phylactery.data.dimension == dimension))) {
                    reincarnate_index = i;
                    break;
                }
            }
        }
        
        if (reincarnate_index != -1) {
            if (pos == null) {
                for (int i = phylacteries.size() - 1; i > reincarnate_index; --i) {
                    Phylactery phylactery = phylacteries.get(i);
                    if (phylactery.data.state == ICapabilityWorldHumanity.State.ACTIVE) {
                        return phylactery;
                    }
                }
            }
            else {
                for (int i = reincarnate_index + 1; i < phylacteries.size(); ++i) {
                    Phylactery phylactery = phylacteries.get(i);
                    if (phylactery.data.state == ICapabilityWorldHumanity.State.ACTIVE &&
                            (phylactery.data.pos.equals(pos) && phylactery.data.dimension == dimension)) {
                        return phylactery;
                    }
                }
            }
        }
        
        return null;
    }
    
    @Override
    public @Nullable Phylactery getPlayerPhylactery(UUID lifetimeUUID, UUID playerUUID) {
        return getPhylacteryInternal(lifetimeUUID, playerUUID, null, 0);
    }
    
    @Override
    public @Nullable Phylactery getBlockPhylactery(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension) {
        assert(pos != null);
        return getPhylacteryInternal(lifetimeUUID, playerUUID, pos, dimension);
    }

    @Override
    public State getPhylacteryState(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension) {
        Phylactery phylactery = getPhylacteryInternal(lifetimeUUID, playerUUID, pos, dimension);
        if (phylactery == null) {
            return ICapabilityWorldHumanity.State.DEACTIVATED;
        }
        return phylactery.data.state;
    }

    @Override
    public void setPhylacteryState(UUID lifetimeUUID, UUID playerUUID, BlockPos pos, int dimension, State state) {
        Phylactery phylactery = getPhylacteryInternal(lifetimeUUID, playerUUID, pos, dimension);
        if (phylactery == null) {
            return;
        }
        phylactery.data.state = state;
    }

    @Override
    public boolean hasPlayerPhylactery(UUID lifetimeUUID, UUID playerUUID) {
        Phylactery phylactery = getPhylacteryInternal(lifetimeUUID, playerUUID, null, 0);
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
            if (phy.lifetimeUUID.equals(lifetimeUUID) && phy.playerUUID.equals(playerUUID) &&
                    phy.data.pos.equals(pos) && phy.data.dimension == dimension) {
                phylacteries.remove(i);
                --entryCount;
                removed = true;
            }
            
        }
        return removed;
    }

    @Override
    public Phylactery[] dumpPhylacteries() {
        Phylactery[] locations = new Phylactery[entryCount];
        int i = 0;
        for (ArrayList<Phylactery> locs : playerUUIDToPhylactery.values()) {
            for (Phylactery loc : locs) {
                locations[i++] = loc;
            }
        }
        return locations;
    }

    @Override
    public void clearAndPutPhylacteries(Phylactery[] phylacteries) {
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
