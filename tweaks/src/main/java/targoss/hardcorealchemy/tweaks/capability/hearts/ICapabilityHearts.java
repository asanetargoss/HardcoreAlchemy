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
