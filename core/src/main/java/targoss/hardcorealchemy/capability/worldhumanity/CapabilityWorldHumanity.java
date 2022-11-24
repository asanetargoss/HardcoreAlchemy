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

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

public class CapabilityWorldHumanity implements ICapabilityWorldHumanity {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "world_humanity");
    
    protected int entryCount = 0;
    protected static final int MAX_LOCATIONS_PER_PLAYER = 5;
    protected HashMap<UUID, ArrayList<MorphAbilityLocation>> playerUUIDToLocation = new HashMap<>();

    protected void registerMorphAbilityLocation(MorphAbilityLocation location) {
        ArrayList<MorphAbilityLocation> locations = playerUUIDToLocation.get(location.playerUUID);
        if (locations == null) {
            locations = new ArrayList<>(1);
            playerUUIDToLocation.put(location.playerUUID, locations);
        }
        for (int i = 0; i < locations.size(); ++i) {
            MorphAbilityLocation loc = locations.get(i);
            if (loc.lifetimeUUID.equals(location.lifetimeUUID) && loc.playerUUID.equals(location.playerUUID)) {
                // Already exists. Remove from list
                locations.remove(i);
                --entryCount;
            }
        }
        if (locations.size() > MAX_LOCATIONS_PER_PLAYER) {
            // Trim the array, discarding earlier entries as they are considered the oldest locations.
            for (int toRemove = locations.size() - MAX_LOCATIONS_PER_PLAYER; toRemove > 0; --toRemove) {
                locations.remove(0);
                --entryCount;
            }
        }
        if (entryCount == Integer.MAX_VALUE) {
            return;
        }
        locations.add(location);
        ++entryCount;
    }

    @Override
    public void registerMorphAbilityLocation(UUID lifetimeUUID, UUID playerUUID, BlockPos pos) {
        registerMorphAbilityLocation(new MorphAbilityLocation(lifetimeUUID, playerUUID, pos));
    }

    @Override
    public boolean unregisterMorphAbilityLocation(UUID lifetimeUUID, UUID playerUUID, BlockPos pos) {
        ArrayList<MorphAbilityLocation> locations = playerUUIDToLocation.get(playerUUID);
        if (locations == null) {
            return false;
        }
        boolean removed = false;
        for (int i = 0; i < locations.size(); ++i) {
            MorphAbilityLocation loc = locations.get(i);
            if (loc.lifetimeUUID.equals(lifetimeUUID) && loc.playerUUID.equals(playerUUID)) {
                locations.remove(i);
                --entryCount;
                removed = true;
            }
            
        }
        return removed;
    }

    @Override
    public BlockPos getMorphAbilityLocation(UUID lifetimeUUID, UUID playerUUID) {
        ArrayList<MorphAbilityLocation> locations = playerUUIDToLocation.get(playerUUID);
        if (locations == null) {
            return null;
        }
        for (int i = 0; i < locations.size(); ++i) {
            MorphAbilityLocation loc = locations.get(i);
            if (loc.lifetimeUUID.equals(lifetimeUUID) && loc.playerUUID.equals(playerUUID)) {
                return loc.pos;
            }
        }
        return null;
    }

    @Override
    public MorphAbilityLocation[] dumpMorphAbilityLocations() {
        MorphAbilityLocation[] locations = new MorphAbilityLocation[entryCount];
        int i = 0;
        for (ArrayList<MorphAbilityLocation> locs : playerUUIDToLocation.values()) {
            for (MorphAbilityLocation loc : locs) {
                locations[i++] = loc;
            }
        }
        return locations;
    }

    @Override
    public void clearAndPutMorphAbilityLocations(MorphAbilityLocation[] morphAbilityLocations) {
        playerUUIDToLocation.clear();
        entryCount = 0;
        for (MorphAbilityLocation loc : morphAbilityLocations) {
            registerMorphAbilityLocation(loc);
        }
        for (ArrayList<MorphAbilityLocation> locs : playerUUIDToLocation.values()) {
            locs.trimToSize();
        }
    }
}