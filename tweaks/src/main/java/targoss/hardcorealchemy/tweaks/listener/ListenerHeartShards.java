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

package targoss.hardcorealchemy.tweaks.listener;

import java.util.ArrayList;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import targoss.hardcorealchemy.heart.Heart;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.tweaks.capability.hearts.ICapabilityHearts;
import targoss.hardcorealchemy.tweaks.capability.hearts.ICapabilityHearts.ShardProgress;
import targoss.hardcorealchemy.tweaks.item.Items;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.Serialization;

public class ListenerHeartShards extends HardcoreAlchemyListener {
    @CapabilityInject(ICapabilityHearts.class)
    public static final Capability<ICapabilityHearts> HEARTS_CAPABILITY = null;
    
    public static @Nullable ShardProgress getShardProgress(ICapabilityHearts hearts, Heart heart) {
        return hearts.getShardProgressMap().get(heart);
    }
    
    public static ShardProgress getOrInitShardProgress(ICapabilityHearts hearts, Heart heart) {
        ShardProgress shardProgress = hearts.getShardProgressMap().get(heart);
        if (shardProgress == null) {
            shardProgress = new ShardProgress();
            hearts.getShardProgressMap().put(heart, shardProgress);
        }
        return shardProgress;
    }
    
    public static boolean canAcquireHeartShard(EntityPlayer player, ICapabilityHearts hearts, Heart heart) {
        if (hearts.get().contains(heart)) {
            return false;
        }
        return !hearts.getAcquiredShards().contains(heart);
    }
    
    public static boolean acquireHeartShard(EntityPlayer player, ICapabilityHearts hearts, Heart heart) {
        if (!canAcquireHeartShard(player, hearts, heart)) {
            return false;
        }
        boolean added = hearts.getAcquiredShards().add(heart);
        if (added) {
            hearts.getShardProgressMap().remove(heart);
            ItemStack heartStack = new ItemStack(heart.ITEM_SHARD);
            if (!player.inventory.addItemStackToInventory(heartStack)) {
                player.dropItem(heartStack, false);
            }
        }
        return added;
    }
    
    protected static class ShardProgressFlame implements INBTSerializable<NBTTagCompound> {
        // Trial by fire only counts if it's for 3 seconds or longer
        protected static final int MIN_FIRE_DURATION_TICKS = 60;
        
        protected boolean hurtByFire;
        protected int fireTicks;
        protected int fireTime;
        
        public ShardProgressFlame() {
            reset();
        }
        
        public void reset() {
            hurtByFire = false;
            fireTicks = -1;
            fireTime = 0;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            if (!hurtByFire || fireTicks == -1) {
                return null;
            }
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setBoolean("hurt", hurtByFire);
            nbt.setInteger("ticks", fireTicks);
            nbt.setInteger("time", fireTime);
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            hurtByFire = nbt.getBoolean("hurt");
            fireTicks = nbt.hasKey("ticks") ? nbt.getInteger("ticks") : -1;
            fireTime = nbt.getInteger("time");
        }
        
        public void onPlayerBurned(EntityPlayer player) {
            fireTicks = player.fire;
            hurtByFire = true;
        }
        
        public void onPlayerTick(EntityPlayer player, ICapabilityHearts hearts) {
            if (!hurtByFire) {
                return;
            }
            if (player.fire != fireTicks && player.fire != fireTicks - 1) {
                reset();
                return;
            }
            if (player.isPotionActive(MobEffects.FIRE_RESISTANCE)) {
                reset();
                return;
            }
            fireTicks = player.fire;
            if (fireTicks <= 1) {
                if (fireTime > MIN_FIRE_DURATION_TICKS) {
                    acquireHeartShard(player, hearts, Items.HEART_FLAME);
                }
                reset();
            }
            ++fireTime;
        }
        
    }
    
    protected static class ShardProgressHunter implements INBTSerializable<NBTTagCompound> {
        public static final int MAX_KILL_TIME_TICKS = 20 * 60 * 2;
        public static final int TARGET_KILLS = 5;
        
        public static class Kill {
            /** Absolute time since start of kill streak.
             * This should always be 0 for the first entity in the list. */
            public int tick;
            public String entityName;
        }
        
        protected int tick;
        protected ArrayList<Kill> kills;
        
        public ShardProgressHunter() {
            reset();
        }
        
        public void reset() {
            this.tick = 0;
            this.kills = new ArrayList<>();
        }
        
        protected void removeKillsToIndex(int endIndex, Kill killAtEndIndex) {
            int ticksRemoved = killAtEndIndex.tick;
            for (; endIndex >= 0; --endIndex) {
                kills.remove(0);
            }
            tick -= ticksRemoved;
            for (Kill remainingKill : kills) {
                remainingKill.tick -= ticksRemoved;
            }
        }
        
        public void trimForTime() {
            int beforeStreak = this.tick - MAX_KILL_TIME_TICKS;
            if (beforeStreak > 0) {
                int n = kills.size();
                int trimTo = -1;
                for (int i = 0; i < n; ++i) {
                    Kill kill = kills.get(i);
                    if (kill.tick <= beforeStreak) {
                        trimTo = i;
                    } else {
                        break;
                    }
                }
                if (trimTo != -1) {
                    Kill lastTrimKill = kills.get(trimTo);
                    removeKillsToIndex(trimTo, lastTrimKill);
                }
            }
        }
        
        public void addKill(Kill kill) {
            int n = kills.size();
            for (int i = n - 1; i >= 0; --i) {
                Kill checkedKill = kills.get(i);
                if (checkedKill.entityName.equals(kill.entityName)) {
                    removeKillsToIndex(i, checkedKill);
                    break;
                }
            }
            if (kills.size() == 0) {
                tick = 0;
            }
            kill.tick = tick;
            kills.add(kill);
        }

        @Override
        public NBTTagCompound serializeNBT() {
            if (tick == 0 || kills.isEmpty()) {
                return null;
            }
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger("tick", tick);
            NBTTagList nbtKills = new NBTTagList();
            for (Kill hunted : kills) {
                NBTTagCompound nbtHunted = new NBTTagCompound();
                nbtHunted.setInteger("tick", hunted.tick);
                nbtHunted.setString("name", hunted.entityName);
                nbtKills.appendTag(nbtHunted);
            }
            nbt.setTag("kills", nbtKills);
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            tick = nbt.getInteger("tick");
            if (nbt.hasKey("kills", Serialization.NBT_LIST_ID)) {
                NBTTagList nbtKills = nbt.getTagList("kills", Serialization.NBT_LIST_ID);
                if (nbtKills.getTagType() == Serialization.NBT_COMPOUND_ID) {
                    int n = nbtKills.tagCount();
                    for (int i = 0; i < n; ++i) {
                        NBTTagCompound nbtKill = nbtKills.getCompoundTagAt(i);
                        Kill kill = new Kill();
                        kill.tick = nbtKill.getInteger("tick");
                        kill.entityName = nbtKill.getString("name");
                        if (kill.entityName != null) {
                            kills.add(kill);
                        }
                    }
                }
            }
        }

        public void onPlayerTick(EntityPlayer player) {
            if (kills.isEmpty()) {
                return;
            }
            ++tick;
            if (tick > MAX_KILL_TIME_TICKS) {
                trimForTime();
                if (kills.isEmpty()) {
                    reset();
                }
                return;
            }
        }
        
        public void onPlayerKill(EntityPlayer player, ICapabilityHearts hearts, EntityLivingBase killedEntity) {
            // Only track kill if this mob is hostile
            if (!(killedEntity instanceof EntityLiving) || !EntityUtil.isHostileMob((EntityLiving)killedEntity)) {
                return;
            }
            String killedEntityString = EntityList.CLASS_TO_NAME.get(killedEntity.getClass());
            Kill kill = new Kill();
            kill.entityName = killedEntityString;
            addKill(kill);
            if (kills.size() >= TARGET_KILLS) {
                acquireHeartShard(player, hearts, Items.HEART_HUNTER);
                reset();
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerBurned(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer)event.getEntityLiving();
        if (player.world.isRemote) {
            return;
        }
        if (!event.getSource().damageType.equals(DamageSource.onFire.damageType)) {
            return;
        }
        ICapabilityHearts hearts = player.getCapability(HEARTS_CAPABILITY, null);
        if (hearts == null) {
            return;
        }
        
        if (canAcquireHeartShard(player, hearts, Items.HEART_FLAME)) {
            ShardProgress shardProgress = getOrInitShardProgress(hearts, Items.HEART_FLAME);
            ShardProgressFlame flameProgress;
            if (!(shardProgress.getObject() instanceof ShardProgressFlame)) {
                flameProgress = new ShardProgressFlame();
                shardProgress.setObject(flameProgress);
            } else {
                flameProgress = (ShardProgressFlame)shardProgress.getObject();
            }
            flameProgress.onPlayerBurned(player);
        }
    }
    
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (event.player.world.isRemote) {
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        ICapabilityHearts hearts = event.player.getCapability(HEARTS_CAPABILITY, null);
        if (hearts == null) {
            return;
        }
        if (hearts.getShardProgressMap().isEmpty()) {
            return;
        }
        
        if (canAcquireHeartShard(event.player, hearts, Items.HEART_FLAME)) {
            ShardProgress shardProgress = getShardProgress(hearts, Items.HEART_FLAME);
            if (shardProgress != null) {
                if (shardProgress.getObject() instanceof ShardProgressFlame) {
                    ShardProgressFlame flameProgress = (ShardProgressFlame)shardProgress.getObject();
                    flameProgress.onPlayerTick(event.player, hearts);
                }
            }
        }
        
        if (canAcquireHeartShard(event.player, hearts, Items.HEART_HUNTER)) {
            ShardProgress shardProgress = getShardProgress(hearts, Items.HEART_HUNTER);
            if (shardProgress != null) {
                if (shardProgress.getObject() instanceof ShardProgressHunter) {
                    ShardProgressHunter hunterProgress = (ShardProgressHunter)shardProgress.getObject();
                    hunterProgress.onPlayerTick(event.player);
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerKill(LivingDeathEvent event) {
        EntityLivingBase killedEntity = event.getEntityLiving();
        if (killedEntity.world.isRemote) {
            return;
        }
        Entity killerEntity = event.getSource().getEntity();
        if (!(killerEntity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer killerPlayer = (EntityPlayer)killerEntity;
        ICapabilityHearts hearts = killerPlayer.getCapability(HEARTS_CAPABILITY, null);
        if (hearts == null) {
            return;
        }
        
        if (killedEntity instanceof EntityGhast) {
            acquireHeartShard(killerPlayer, hearts, Items.HEART_TEARS);
        }
        
        if (canAcquireHeartShard(killerPlayer, hearts, Items.HEART_HUNTER)) {
            ShardProgress shardProgress = getOrInitShardProgress(hearts, Items.HEART_HUNTER);
            ShardProgressHunter hunterProgress;
            if (!(shardProgress.getObject() instanceof ShardProgressHunter)) {
                hunterProgress = new ShardProgressHunter();
                shardProgress.setObject(hunterProgress);
            }
            else {
                hunterProgress = (ShardProgressHunter)shardProgress.getObject();
            }
            hunterProgress.onPlayerKill(killerPlayer, hearts, killedEntity);
        }
    }
}
