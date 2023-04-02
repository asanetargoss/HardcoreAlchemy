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

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import targoss.hardcorealchemy.heart.Heart;

public interface ICapabilityHearts {
    Set<Heart> get();
    void set(Set<Heart> hearts);
    List<ResourceLocation> getRemoved();
    void setRemoved(List<ResourceLocation> removedHearts);
    /** Per life */
    Set<Heart> getAcquiredShards();
    void setAcquiredShards(Set<Heart> acquiredShards);
    
    public static class ShardProgress {
        protected NBTTagCompound nbt;
        protected INBTSerializable<NBTTagCompound> object;
        
        public void setObject(INBTSerializable<NBTTagCompound> object) {
            this.object = object;
            this.nbt = null;
        }
        
        public void setNBT(NBTTagCompound nbt) {
            this.nbt = nbt;
            if (object != null) {
                object.deserializeNBT(nbt);
            }
        }
        
        public NBTTagCompound deserializeNBT() {
            if (object != null) {
                nbt = object.serializeNBT();
            }
            return nbt;
        }
        
        public INBTSerializable<NBTTagCompound> getObject() {
            if (nbt != null && object != null) {
                object.deserializeNBT(nbt);
                nbt = null;
            }
            return object;
        }
    }
    
    Map<Heart, ShardProgress> getShardProgressMap();
    void setShardProgressMap(Map<Heart, ShardProgress> shardProgressMap);
}
