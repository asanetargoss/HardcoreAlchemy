package targoss.hardcorealchemy.capability.worldhumanity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import net.minecraft.util.math.Vec3i;

public class CapabilityWorldHumanity implements ICapabilityWorldHumanity {
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
    public void registerMorphAbilityLocation(UUID lifetimeUUID, UUID playerUUID, Vec3i pos) {
        registerMorphAbilityLocation(new MorphAbilityLocation(lifetimeUUID, playerUUID, pos));
    }

    @Override
    public boolean unregisterMorphAbilityLocation(UUID lifetimeUUID, UUID playerUUID, Vec3i pos) {
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
    public Vec3i getMorphAbilityLocation(UUID lifetimeUUID, UUID playerUUID) {
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
