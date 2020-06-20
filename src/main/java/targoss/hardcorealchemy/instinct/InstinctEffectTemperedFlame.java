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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.instinct.api.IInstinctEffectData;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.listener.ListenerInstinctOverheat;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.WorldUtil;
import targoss.hardcorealchemy.util.WorldUtil.BlockInfo;

/** Player is too cold to function unless regularly exposed to flame */
public class InstinctEffectTemperedFlame extends InstinctEffect {
    @CapabilityInject(ICapabilityInstinct.class)
    private static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;
    
    /*  The overheat effect lasts 2 days, so we need to make sure we don't overlap.
     *  Note also, the overheat amplifier is usually lower when
     *  we're paired with InstinctEffectNetherFever, so the gap is more than 2 days.
     *  Note also, we do check if we're overheating, and not apply cold effects in that case.
     */
    public static final int UNTIL_COLD_TIME_PER_AMP = 3 * 12000;
    /* When cold effects start, 4 days to max at amplifier 1, 2 days at amplifier 2 */
    public static final float COLD_PER_DAY_PER_AMP = 0.25F;
    /* Per tick AFTER we pass the threshold */
    public static final float COLD_PER_TICK_PER_AMP = COLD_PER_DAY_PER_AMP / 12000F;

    public static class Data implements IInstinctEffectData {
        public Random random = new Random();
        /** Larger means cold effects will be worse (measured in ticks) */
        public int coolingTime;
        
        protected void updateTimers() {
            if (coolingTime < Integer.MAX_VALUE) {
                ++coolingTime;
            }
        }
        
        protected static final String NBT_COOLING_TIME = "cooling_time";

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger(NBT_COOLING_TIME, coolingTime);
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            if (nbt.hasKey(NBT_COOLING_TIME)) {
                coolingTime = nbt.getInteger(NBT_COOLING_TIME);
            }
        }
    }
    
    @Override
    public IInstinctEffectData createData() {
        return new Data();
    }
    
    protected static boolean isIcyBlock(Block block, @Nullable IBlockState state) {
        @SuppressWarnings("deprecation")
        Material steppingMaterial = block.getMaterial(state != null ? state : block.getDefaultState());
        boolean icy = (steppingMaterial == Material.ICE ||
                steppingMaterial == Material.PACKED_ICE ||
                steppingMaterial == Material.SNOW ||
                steppingMaterial == Material.CRAFTED_SNOW);
        return icy;
    }
    
    protected static class ColdAmplifier {
        public final float required;
        public final float amount;
        public final Predicate<ItemStack> condition;
        public ColdAmplifier(float required, float amount, Predicate<ItemStack> condition) {
            this.required = required;
            this.amount = amount;
            this.condition = condition;
        }
        public ColdAmplifier(float required, float amount) {
            this(required, amount, null);
        }
    }
    protected static final Map<ResourceLocation, ColdAmplifier> coldAmplifiers = new HashMap<>();
    static {
        Map<ResourceLocation, ColdAmplifier> c = coldAmplifiers;
        {
            ColdAmplifier veryCold = new ColdAmplifier(0.0F, 1.0F);
            c.put(new ResourceLocation("snow"), veryCold);
            c.put(new ResourceLocation("snow_layer"), veryCold);
            c.put(new ResourceLocation("snowball"), veryCold);
            c.put(new ResourceLocation("ice"), veryCold);
            c.put(new ResourceLocation("packed_ice"), veryCold);
            c.put(new ResourceLocation("frosted_ice"), veryCold);
        }
        {
            ColdAmplifier prettyCold = new ColdAmplifier(1.0F, 0.9F);
            c.put(new ResourceLocation("potion"), prettyCold);
            c.put(new ResourceLocation("harvestcraft:freshwateritem"), prettyCold);
        }
        {
            ColdAmplifier cool = new ColdAmplifier(2.0F, 0.5F);
            c.put(new ResourceLocation("alchemicash:CrystalCatalyst"), cool);
            c.put(new ResourceLocation("alchemicash:Skystone"), cool);
            c.put(new ResourceLocation("alchemicash:Skystone2"), cool);
            c.put(new ResourceLocation("mysticalagriculture:water_essence"), cool);
            c.put(new ResourceLocation("villagebox:water_shard"), cool);
            c.put(new ResourceLocation("adinferos:golden_bucket_water"), cool);
        }
    }

    /** This function assumes itemStack is valid and non-empty */
    public float getItemColdAmplifier(EntityPlayer player, ItemStack itemStack, float amplifier) {
        Item item = itemStack.getItem();

        // A filled bucket, tank, etc is "cold" for a nether being if its temperature is less than the boiling point of water.
        // And a bucket of water (temperature 300) is defined to have a cold amplifier of 0.9.
        if (amplifier >= 1.0F) {
            IFluidHandler bucket = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            if (bucket != null) {
                IFluidTankProperties[] fluids = bucket.getTankProperties();
                for (IFluidTankProperties fluidProperties : fluids) {
                    FluidStack fluidStack = fluidProperties.getContents();
                    if (InventoryUtil.isEmptyFluidStack(fluidStack)) {
                        continue;
                    }
                    Fluid fluid = fluidStack.getFluid();
                    int temperature = fluid.getTemperature(fluidStack);
                    if (temperature < 373) {
                        return (0.9F * (373.0F - (float)temperature) / 73.0F) * amplifier;
                    }
                }
                
            }
        }

        // If it's not a fluid, then look at the item name
        ResourceLocation itemResource = item.getRegistryName();
        if (coldAmplifiers.containsKey(itemResource)) {
            ColdAmplifier coldAmplifier = coldAmplifiers.get(itemResource);
            if (coldAmplifier.required <= amplifier &&
                    (coldAmplifier.condition == null || coldAmplifier.condition.test(itemStack))) {
                return coldAmplifier.amount * amplifier;
            }
        }

        return 0.0F;
    }
    
    public float getColdAmplifier(EntityPlayer player, Data data, float amplifier) {
        // Check for cold sources.
        // Sensitivity depends on how long it has been since the player has been exposed to heat,
        //   the current effect amplifier,
        //   and the coldness of various nearby things.
        // The higher the amplifier, the more things can make the player cold,
        //   and the more cold those things feel.
        float maxAllowedColdAmplifier = Math.max(0.0F, 
                (data.coolingTime - (UNTIL_COLD_TIME_PER_AMP * amplifier)) * COLD_PER_TICK_PER_AMP * amplifier
                );
        if (maxAllowedColdAmplifier <= 0.0F) {
            return 0.0F;
        }
        float baseColdAmplifier = 0.0F;

        // Check if the player is standing on a cold block
        if (amplifier >= 0.5F) {
            // x position is off by -1; some sort of rounding issue
            BlockPos steppingPos = new BlockPos((int)(player.posX - 1), (int)Math.floor(player.posY - 0.2), (int)player.posZ);
            IBlockState steppingState = player.world.getBlockState(steppingPos);
            if (steppingState != null) {
                Block steppingBlock = steppingState.getBlock();
                boolean standingOnColdBlock = isIcyBlock(steppingBlock, steppingState);
                // Also check one block above
                if (!standingOnColdBlock) {
                    IBlockState snowLayerState = player.world.getBlockState(steppingPos.up());
                    if (snowLayerState != null) {
                        Block snowLayerBlock = snowLayerState.getBlock();
                        if (snowLayerBlock != null) {
                            @SuppressWarnings("deprecation")
                            boolean isOnSnowLayer = snowLayerBlock.getMaterial(snowLayerState) == Material.SNOW;
                            standingOnColdBlock |= isOnSnowLayer;
                        }
                    }
                }
                if (standingOnColdBlock) {
                    baseColdAmplifier = Math.max(baseColdAmplifier, amplifier * 1.0F);
                }
            }
        }

        // Check if the player is in water or rain
        if (amplifier >= 1.25F) {
            if (player.isInWater()) {
                baseColdAmplifier = Math.max(baseColdAmplifier, amplifier * 0.9F);
            }
            BlockPos playerPos = player.getPosition();
            if (player.world.canSeeSky(playerPos) && player.world.isRainingAt(playerPos)) {
                baseColdAmplifier = Math.max(baseColdAmplifier, amplifier * 0.5F);
            }
        }

        // Check if the player is holding cold items in their inventory
        // Items inside of backpacks don't count
        if (amplifier >= 2.0F) {
            float maxItemAmplifier = 0.0F;
            InventoryPlayer inventory = player.inventory;
            int n = inventory.getSizeInventory();
            for (int i = 0; i < n; i++) {
                ItemStack itemStack = inventory.getStackInSlot(i);
                if (InventoryUtil.isEmptyItemStack(itemStack)) {
                    continue;
                }
                float itemAmplifier = getItemColdAmplifier(player, itemStack, amplifier);
                maxItemAmplifier = Math.max(maxItemAmplifier, itemAmplifier);
            }
            baseColdAmplifier = Math.max(baseColdAmplifier, maxItemAmplifier);
        }

        float coldAmplifier = Math.min(baseColdAmplifier, maxAllowedColdAmplifier);
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
                BlockInfo fireInfo = WorldUtil.getRandomCollidingState(data.random,
                        player, 
                        WorldUtil.CollisionPredicate.FIRE,
                        WorldUtil.CollisionMethod.FIRE);
                if (fireInfo != null) {
                    player.world.setBlockToAir(fireInfo.getPos());
                    BlockPos fuelPos = WorldUtil.getFireFuelPos(player.world, fireInfo.getPos());
                    if (data.random.nextFloat() <= 0.25F) {
                        player.world.setBlockToAir(fuelPos);
                    }
                }
            }

            BlockInfo lavaInfo = WorldUtil.getRandomCollidingState(data.random,
                    player,
                    WorldUtil.CollisionPredicate.LAVA,
                    WorldUtil.CollisionMethod.LAVA);
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
    
    public static boolean needsHeat(Data data, float amplifier) {
        return data.coolingTime >= (UNTIL_COLD_TIME_PER_AMP * amplifier);
    }
    
    protected static void exposeToHeat(EntityPlayer player, Data data, float amplifier) {
        assert(!player.world.isRemote);
        if (!player.world.isRemote) {
            if (needsHeat(data, amplifier)) {
                Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.effect.tempered_flame.fulfilled"));
            }
        }
        data.coolingTime = 0;
    }

    @Override
    public void onActivate(EntityPlayer player, float amplifier) {}

    @Override
    public void onDeactivate(EntityPlayer player, float amplifier) {
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        Data data = (Data)instinct.getInstinctEffectData(this);
        data.coolingTime = 0;
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
            if (needsHeat(data, amplifier)) {
                consumeLesserHeat(player, data);
            }
            exposeToHeat(player, data, amplifier);
        }
        else if (!ListenerInstinctOverheat.isOverheating(player)) {
            float coldAmplifier = getColdAmplifier(player, data, amplifier);
            applyColdEffects(player, coldAmplifier);
        }
        data.updateTimers();
    }

}
