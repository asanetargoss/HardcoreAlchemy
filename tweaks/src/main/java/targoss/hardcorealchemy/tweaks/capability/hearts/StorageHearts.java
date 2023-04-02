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

import static targoss.hardcorealchemy.item.Items.HEART_REGISTRY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import targoss.hardcorealchemy.heart.Heart;
import targoss.hardcorealchemy.tweaks.capability.hearts.ICapabilityHearts.ShardProgress;
import targoss.hardcorealchemy.util.Serialization;

public class StorageHearts implements Capability.IStorage<ICapabilityHearts> {
    protected static final String HEARTS = "hearts";
    protected static final String REMOVED_HEARTS = "removed_hearts";
    protected static final String ACQUIRED_SHARDS = "acquired_shards";
    public static final String SHARD_PROGRESS = "shard_progress";
    protected static final String SHARD_PROGRESS_ID = "id";
    protected static final String SHARD_PROGRESS_DATA = "data";
    
    @Override
    public NBTBase writeNBT(Capability<ICapabilityHearts> capability, ICapabilityHearts instance, EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        
        {
            Set<Heart> hearts = instance.get();
            if (hearts.size() != 0) {
                NBTTagList nbtHearts = new NBTTagList();
                for (Heart heart : hearts) {
                    nbtHearts.appendTag(new NBTTagString(heart.getRegistryName().toString()));
                }
                nbt.setTag(HEARTS, nbtHearts);
            }
        }
        {
            List<ResourceLocation> removedHearts = instance.getRemoved();
            if (removedHearts.size() != 0) {
                NBTTagList nbtRemovedHearts = new NBTTagList();
                for (ResourceLocation removedHeart : removedHearts) {
                    nbtRemovedHearts.appendTag(new NBTTagString(removedHeart.toString()));
                }
                nbt.setTag(REMOVED_HEARTS, nbtRemovedHearts);
            }
        }
        {
            Set<Heart> acquiredShards = instance.getAcquiredShards();
            if (acquiredShards.size() != 0) {
                NBTTagList nbtAcquiredShards = new NBTTagList();
                for (Heart acquiredShard : acquiredShards) {
                    nbtAcquiredShards.appendTag(new NBTTagString(acquiredShard.getRegistryName().toString()));
                }
                nbt.setTag(ACQUIRED_SHARDS, nbtAcquiredShards);
            }
        }
        {
            Map<Heart, ShardProgress> shardProgressMap = instance.getShardProgressMap();
            if (shardProgressMap.size() != 0) {
                NBTTagList nbtShardProgressList = new NBTTagList();
                for (Map.Entry<Heart, ShardProgress> entry : shardProgressMap.entrySet()) {
                    NBTTagCompound nbtShardProgress = new NBTTagCompound();
                    String shardProgressId = entry.getKey().getRegistryName().toString();
                    nbtShardProgress.setString(SHARD_PROGRESS_ID, shardProgressId);
                    NBTTagCompound shardProgressNbt = null;
                    try {
                        shardProgressNbt = entry.getValue().deserializeNBT();
                    } catch (Exception e) {}
                    if (shardProgressNbt == null) {
                        continue;
                    }
                    nbtShardProgress.setTag(SHARD_PROGRESS_DATA, shardProgressNbt);
                    nbtShardProgressList.appendTag(nbtShardProgress);
                }
                nbt.setTag(SHARD_PROGRESS, nbtShardProgressList);
            }
        }

        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityHearts> capability, ICapabilityHearts instance, EnumFacing side,
            NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        

        Set<Heart> hearts = new HashSet<>();
        List<ResourceLocation> removedHearts = new ArrayList<>();
        Set<Heart> acquiredShards = new HashSet<>();
        Map<Heart, ShardProgress> shardProgressMap = new HashMap<>();
        
        if (nbtCompound.hasKey(HEARTS, Serialization.NBT_LIST_ID)) {
            try {
                NBTTagList nbtHearts = nbtCompound.getTagList(HEARTS, Serialization.NBT_STRING_ID);
                int n = nbtHearts.tagCount();
                for (int i = 0; i < n; ++i) {
                    String heartId = nbtHearts.getStringTagAt(i);
                    if (heartId == null) {
                        continue;
                    }
                    Heart heart = HEART_REGISTRY.getValue(new ResourceLocation(heartId));
                    if (heart == null) {
                        continue;
                    }
                    hearts.add(heart);
                }
            } catch (ReportedException e) {}
        }
        if (nbtCompound.hasKey(REMOVED_HEARTS, Serialization.NBT_LIST_ID)) {
            try {
                NBTTagList nbtRemovedHearts = nbtCompound.getTagList(REMOVED_HEARTS, Serialization.NBT_STRING_ID);
                int n = nbtRemovedHearts.tagCount();
                for (int i = 0; i < n; ++i) {
                    String removedHeartId = nbtRemovedHearts.getStringTagAt(i);
                    if (removedHeartId == null) {
                        continue;
                    }
                    removedHearts.add(new ResourceLocation(removedHeartId));
                }
            } catch (ReportedException e) {}
        }
        if (nbtCompound.hasKey(ACQUIRED_SHARDS, Serialization.NBT_LIST_ID)) {
            try {
                NBTTagList nbtAcquiredShards = nbtCompound.getTagList(ACQUIRED_SHARDS, Serialization.NBT_STRING_ID);
                int n = nbtAcquiredShards.tagCount();
                for (int i = 0; i < n; ++i) {
                    String acquiredShardId = nbtAcquiredShards.getStringTagAt(i);
                    if (acquiredShardId == null) {
                        continue;
                    }
                    Heart acquiredShard = HEART_REGISTRY.getValue(new ResourceLocation(acquiredShardId));
                    if (acquiredShard == null) {
                        continue;
                    }
                    acquiredShards.add(acquiredShard);
                }
            } catch (ReportedException e) {}
        }
        if (nbtCompound.hasKey(SHARD_PROGRESS, Serialization.NBT_LIST_ID)) {
            try {
                NBTTagList nbtShardProgressList = nbtCompound.getTagList(SHARD_PROGRESS, Serialization.NBT_COMPOUND_ID);
                int n = nbtShardProgressList.tagCount();
                for (int i = 0; i < n; ++i) {
                    NBTTagCompound nbtShardProgress = nbtShardProgressList.getCompoundTagAt(i);
                    String nbtShardId = nbtShardProgress.getString(SHARD_PROGRESS_ID);
                    if (nbtShardId == null) {
                        continue;
                    }
                    Heart shardWithProgress = HEART_REGISTRY.getValue(new ResourceLocation(nbtShardId));
                    if (shardWithProgress == null) {
                        continue;
                    }
                    if (!nbtShardProgress.hasKey(SHARD_PROGRESS_DATA, Serialization.NBT_COMPOUND_ID)) {
                        continue;
                    }
                    NBTTagCompound nbtShardProgressValue = nbtShardProgress.getCompoundTag(SHARD_PROGRESS_DATA);
                    ShardProgress shardProgress = new ShardProgress();
                    shardProgress.setNBT(nbtShardProgressValue);
                    shardProgressMap.put(shardWithProgress, shardProgress);
                }

            } catch (ReportedException e) {}
        }
        
        instance.set(hearts);
        instance.setRemoved(removedHearts);
        instance.setAcquiredShards(acquiredShards);
        instance.setShardProgressMap(shardProgressMap);
    }
}
