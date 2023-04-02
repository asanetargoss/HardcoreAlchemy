/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.capability.hearts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.heart.Heart;

public class CapabilityHearts implements ICapabilityHearts {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "hearts");
    
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
