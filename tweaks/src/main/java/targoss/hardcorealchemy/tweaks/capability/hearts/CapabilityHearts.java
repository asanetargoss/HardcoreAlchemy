package targoss.hardcorealchemy.tweaks.capability.hearts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.heart.Heart;

public class CapabilityHearts implements ICapabilityHearts {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "hearts");
    
    protected Set<Heart> hearts = new HashSet<>();
    protected List<ResourceLocation> removedHearts = new ArrayList<>();
    protected Set<Heart> acquiredShards = new HashSet<>();
    protected Map<Heart, ShardProgress> shardProgressMap = new HashMap<>();
    
    @Override
    public Set<Heart> get() {
        return hearts;
    }

    @Override
    public void set(Set<Heart> hearts) {
        this.hearts = hearts;
    }

    @Override
    public List<ResourceLocation> getRemoved() {
        return removedHearts;
    }

    @Override
    public void setRemoved(List<ResourceLocation> removedHearts) {
        this.removedHearts = removedHearts;
    }

    @Override
    public Set<Heart> getAcquiredShards() {
        return acquiredShards;
    }

    @Override
    public void setAcquiredShards(Set<Heart> acquiredShards) {
        this.acquiredShards = acquiredShards;
    }

    @Override
    public Map<Heart, ShardProgress> getShardProgressMap() {
        return shardProgressMap;
    }

    @Override
    public void setShardProgressMap(Map<Heart, ShardProgress> shardProgressMap) {
        this.shardProgressMap = shardProgressMap;
    }
}
