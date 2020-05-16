/*
 * Copyright 2020 asanetargoss
 * 
 * This file is part of Hardcore Alchemy.
 * 
 * Hardcore Alchemy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 * 
 * Hardcore Alchemy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hardcore Alchemy.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.instinct;

import java.util.Random;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.instinct.api.IInstinctEffectData;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.listener.ListenerInstinctOverheat;
import targoss.hardcorealchemy.util.WorldUtil;
import targoss.hardcorealchemy.util.WorldUtil.BlockInfo;

/** Player is too cold to function unless regularly exposed to flame */
public class InstinctEffectTemperedFlame extends InstinctEffect {
    @CapabilityInject(ICapabilityInstinct.class)
    private static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;
    
    /*  Overheat is 2 days, so we need to make sure we don't overlap.
     *  Note also, the overheat amplifier is usually lower when
     *  we're paired with InstinctEffectNetherFever, so the gap is more than 2 days.
     *  Note also, we do check if we're overheating, and not apply cold effects in that case.
     */
    public static final int UNTIL_COLD_TIME_PER_AMP = 3 * 12000;
    /* When cold effects start, 4 days to max at amplifier 1, 2 days at amplifier 2 */
    public static final float COLD_PER_DAY_PER_AMP = 0.25F;
    /* Per tick AFTER we pass the threshold */
    public static final float COLD_PER_TICK_PER_AMP = COLD_PER_DAY_PER_AMP / 12000F;

    protected static class Data implements IInstinctEffectData {
        public Random random = new Random();
        /** Larger means cold effects will be worse (measured in ticks) */
        public int coolingTime;
        public int maxCoolingTime;
        
        protected void updateTimers() {
            if (coolingTime < maxCoolingTime) {
                ++coolingTime;
            }
        }

        @Override
        public NBTTagCompound serializeNBT() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            // TODO Auto-generated method stub
            
        }
    }
    
    @Override
    public IInstinctEffectData createData() {
        return new Data();
    }

    public float getItemColdAmplifier(EntityPlayer player, ItemStack itemStack, float amplifier) {
        // TODO: Depends on the item...
        /*
        if (ice/snow) {
            return amplifier * 1.0F;
        }
        if (water/potion) {
            return amplifier * 0.9F;
        }
        if (water essence, etc) {
            return amplifier * 0.5F;
        }
         */
        return 0.0F;
    }
    
    public float getColdAmplifier(EntityPlayer player, Data data, float amplifier) {
        // TODO: Check for cold sources, sensitivity depends on how long we have been cold + amplifier, returned amplifier depends on sensitivity and the coldness of the object
        // NOTE: For consistency with InstinctEffectOverheat, cooling debuffs should occur after the overheat effects go away (ideally, with a sweet spot in between)
        float maxAllowedColdAmplifier = Math.max(0.0F, 
                (data.coolingTime - (UNTIL_COLD_TIME_PER_AMP * amplifier)) * COLD_PER_TICK_PER_AMP * amplifier
                );
        if (maxAllowedColdAmplifier <= 0.0F) {
            return 0.0F;
        }
        float baseColdAmplifier = 0.0F;
        if (amplifier >= 0.5F) {
            BlockPos steppingPos = new BlockPos((int)player.posX, (int)Math.floor(player.posY - 0.2), (int)player.posZ);
            IBlockState steppingState = player.world.getBlockState(steppingPos);
            if (steppingState != null) {
                Block steppingBlock = steppingState.getBlock();
                //TODO
                /*
                if (standing on/in ice/snow) {
                    baseColdAmplifier = Math.max(baseColdAmplifier, amplifier * 1.0F);
                }
                */
            }
        }
        //TODO
        /*
        if (amplifier >= 1.25F) {
            if (in water) {
                baseColdAmplifier = Math.max(baseColdAmplifier, amplifier * 0.9F);
            }
            if (outside and is raining) {
                baseColdAmplifier = Math.max(baseColdAmplifier, amplifier * 0.5F);
            }
        }
        if (amplifier >= 2.0F) {
            // Items inside of backpacks don't count
            float maxItemAmplifier = 0.0F;
            for (each item in inventory) {
                float itemAmplifier
                maxItemAmplifier = Math.max(maxItemAmplifier, itemAmplifier);
            }
            baseColdAmplifier = Math.max(baseColdAmplifier, maxItemAmplifier);
        }
         */
        float coldAmplifier = Math.max(baseColdAmplifier, maxAllowedColdAmplifier);
        return coldAmplifier;
    }
    
    public static void applyColdEffects(EntityPlayer player, float amplifier) {
        final int effectTime = 10 * 20;
        if (amplifier >= 0.5F) {
            if (amplifier >= 2.0F) {
                // Slowness II
                player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, effectTime, 1));
            }
            else {
                player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, effectTime));
            }
        }
        if (amplifier >= 1.0F) {
            player.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, effectTime));
        }
        if (amplifier >= 1.5F) {
            player.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, effectTime));
        }
    }

    protected static void consumeLesserHeat(EntityPlayer player, Data data) {
        // Consume fire/lava source/fire fuel while NOT in Nether
        // A fire block will always be consumed if in range. Fire fuel/lava have a chance to be consumed.
        // The player will also no longer be on fire (even if in the Nether)
        if (player.dimension != DimensionType.NETHER.getId()) {
            if (player.onGround) {
                BlockInfo fireInfo = WorldUtil.getRandomCollidingState(data.random, player, new Predicate<BlockInfo>() {
                    @Override
                    public boolean test(BlockInfo info) {
                        return info.getState().getBlock() == net.minecraft.init.Blocks.FIRE;
                    }
                }, WorldUtil.CollisionMethod.FIRE);
                if (fireInfo != null) {
                    player.world.setBlockToAir(fireInfo.getPos());
                    BlockPos fuelPos = WorldUtil.getFireFuelPos(player.world, fireInfo.getPos());
                    if (data.random.nextFloat() <= 0.25F) {
                        player.world.setBlockToAir(fuelPos);
                    }
                }
            }

            BlockInfo lavaInfo = WorldUtil.getRandomCollidingState(data.random, player, new Predicate<BlockInfo>() {
                @SuppressWarnings("deprecation")
                @Override
                public boolean test(BlockInfo info) {
                    return info.getState().getBlock().getMaterial(info.getState()) == Material.LAVA;
                }
            }, WorldUtil.CollisionMethod.LAVA);
            if (lavaInfo != null) {
                if (data.random.nextFloat() <= 0.05F) {
                    player.world.setBlockToAir(lavaInfo.getPos());
                }
            }
        }

        player.extinguish();
        player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS,
                0.5F, 2.6F + (data.random.nextFloat() - data.random.nextFloat()) * 0.8F);
    }
    
    public static boolean needsHeat(Data data) {
        // TODO: data.coolingTime > something
        return false;
    }
    
    protected static void exposeToHeat(EntityPlayer player, Data data) {
        // TODO: Message that the player did something good (for now)
        data.coolingTime = 0;
    }

    @Override
    public void onActivate(EntityPlayer player, float amplifier) {
        if (player.world.isRemote) {
            return;
        }

        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        Data data = (Data)instinct.getInstinctEffectData(this);
        
        // Do we really need this? Probably not. Although it's a good sanity check.
        data.maxCoolingTime = (int)(
                (UNTIL_COLD_TIME_PER_AMP * amplifier) +
                (1.0F / (COLD_PER_DAY_PER_AMP * amplifier))
                );
    }

    @Override
    public void onDeactivate(EntityPlayer player, float amplifier) {
        // TODO Auto-generated method stub

    }

    @Override
    public void tick(EntityPlayer player, float amplifier) {
        if (player.world.isRemote) {
            return;
        }

        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        Data data = (Data)instinct.getInstinctEffectData(this);
        if (InstinctEffectNetherFever.isInHeat(player)) {
            if (needsHeat(data)) {
                consumeLesserHeat(player, data);
            }
            exposeToHeat(player, data);
        } else if (!ListenerInstinctOverheat.isOverheating(player)) {
            float coldAmplifier = getColdAmplifier(player, data, amplifier);
            applyColdEffects(player, coldAmplifier);
        }
        data.updateTimers();
    }

}
