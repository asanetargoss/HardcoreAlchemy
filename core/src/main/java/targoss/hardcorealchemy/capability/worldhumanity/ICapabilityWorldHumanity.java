package targoss.hardcorealchemy.capability.worldhumanity;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.util.math.Vec3i;

public interface ICapabilityWorldHumanity {
    /** If a location is already registered for the given lifetimeUUID
     * or playerID, it may be cleared, but this is not guaranteed.
     * As long as player data is in a consistent state, there is a unique
     * (and possibly null) position for the given lifetimeUUID/playerID pair.
     * */
    void registerMorphAbilityLocation(UUID lifetimeUUID, UUID playerUUID, Vec3i pos);
    /** Returns true if there actually was a registered morph ability at the given location */
    boolean unregisterMorphAbilityLocation(UUID lifetimeUUID, UUID playerUUID, Vec3i pos);
    @Nullable Vec3i getMorphAbilityLocation(UUID lifetimeUUID, UUID playerUUID);
    
    public static class MorphAbilityLocation {
        public MorphAbilityLocation(UUID lifetimeUUID, UUID playerUUID, Vec3i pos) {
            this.lifetimeUUID = lifetimeUUID;
            this.playerUUID = playerUUID;
            this.pos = pos;
        }
        public UUID lifetimeUUID;
        public UUID playerUUID;
        public Vec3i pos;
    }
    MorphAbilityLocation[] dumpMorphAbilityLocations();
    void clearAndPutMorphAbilityLocations(MorphAbilityLocation[] morphAbilityLocations);
}
