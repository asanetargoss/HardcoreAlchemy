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
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.oredict.OreDictionary;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.instinct.api.IInstinctEffectData;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.item.Items;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.NamedItem;

/** Exposure to flame causes player to heat/burn things */
public class InstinctEffectOverheat extends InstinctEffect {
    @CapabilityInject(ICapabilityInstinct.class)
    private static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;

    public static final float OVERHEAT_TIME_PER_AMPLIFIER = 5 * 12000;
    public static final int OVERHEAT_EVENT_FREQUENCY = 12000 / 3;
    public static final float OVERHEAT_EVENT_CHANCE = 0.333F;

    public static class Data implements IInstinctEffectData {
        public Random random = new Random();
        /** If >0 then morph is overheating (measured in ticks) */
        public int overheatTimer;
        public int maxOverheatTimer;
        public int nextOverheatEventTime;
        
        public void updateTimers() {
            if (overheatTimer > 0) {
                --overheatTimer;
                overheatTimer = Math.min(overheatTimer, maxOverheatTimer);
                ++nextOverheatEventTime;
            }
            else {
                nextOverheatEventTime = 0;
            }
        }
        
        public static final String NBT_OVERHEAT_TIMER = "overheat";
        public static final String NBT_MAX_OVERHEAT_TIMER = "max_overheat";
        public static final String NBT_NEXT_OVERHEAT_TIME = "event_time";

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setInteger(NBT_OVERHEAT_TIMER, overheatTimer);
            nbt.setInteger(NBT_MAX_OVERHEAT_TIMER, maxOverheatTimer);
            nbt.setInteger(NBT_NEXT_OVERHEAT_TIME, nextOverheatEventTime);
            return nbt;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            overheatTimer = nbt.getInteger(NBT_OVERHEAT_TIMER);
            maxOverheatTimer = nbt.getInteger(NBT_MAX_OVERHEAT_TIMER);
            nextOverheatEventTime = nbt.getInteger(NBT_NEXT_OVERHEAT_TIME);
        }
        
        public boolean isOverheating() {
            return overheatTimer > 0;
        }
    }
    
    @Override
    public IInstinctEffectData createData() {
        return new Data();
    }

    public void exposeToHeat(EntityPlayer player, Data data) {
        assert(!player.world.isRemote);
        if (!player.world.isRemote) {
            if (data.overheatTimer == 0) {
                Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.effect.overheat.exposed"));
            }
        }
        data.overheatTimer = data.maxOverheatTimer;
    }
    
    protected static NamedItem BOTTLE = new NamedItem("glass_bottle");

    protected static ItemStack boilLiquidInItemStack(ItemStack itemStack) {
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return itemStack;
        }

        // If it's a potion, then just convert it to an empty bottle
        if (itemStack.getItem() instanceof ItemPotion) {
            return new ItemStack(BOTTLE.get(), itemStack.stackSize);
        }
        
        // A filled bucket, tank, etc is "cold" for a nether being if its temperature is less than the boiling point of water.
        // And will therefore boil away
        if (itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
            // Copy the stack, don't take risks!
            itemStack = itemStack.copy();
            IFluidHandler bucket = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
            IFluidTankProperties[] fluids = bucket.getTankProperties();
            for (IFluidTankProperties fluidProperties : fluids) {
                FluidStack fluidStack = fluidProperties.getContents();
                if (InventoryUtil.isEmptyFluidStack(fluidStack)) {
                    continue;
                }
                Fluid fluid = fluidStack.getFluid();
                int temperature = fluid.getTemperature(fluidStack);
                if (temperature < 373) {
                    bucket.drain(fluidStack, true);
                }
            }
        }
        return itemStack;
    }

    protected static Set<ResourceLocation> iceMeltables = new HashSet<>();
    static {
        iceMeltables.add(new ResourceLocation("snowball"));
        iceMeltables.add(new ResourceLocation("ice"));
        iceMeltables.add(new ResourceLocation("packed_ice"));
        iceMeltables.add(new ResourceLocation("frosted_ice"));
        iceMeltables.add(new ResourceLocation("snow"));
    }
    protected static ItemStack meltIceInItemStack(ItemStack itemStack) {
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return itemStack;
        }

        Item item = itemStack.getItem();
        ResourceLocation id = item.getRegistryName();
        if (iceMeltables.contains(id)) {
            return InventoryUtil.ITEM_STACK_EMPTY;
        }

        return itemStack;
    }

    protected static Set<String> flammableCraftMaterials = new HashSet<>();
    static {
        flammableCraftMaterials.add("WOOD");
        flammableCraftMaterials.add("LEATHER");
        flammableCraftMaterials.add("WOOL");
    }
    protected static Set<Material> flammableBlockMaterials = new HashSet<>();
    static {
        flammableBlockMaterials.add(Material.WOOD);
        flammableBlockMaterials.add(Material.LEAVES);
        flammableBlockMaterials.add(Material.PLANTS);
        flammableBlockMaterials.add(Material.VINE);
        flammableBlockMaterials.add(Material.CLOTH);
        flammableBlockMaterials.add(Material.CARPET);
        flammableBlockMaterials.add(Material.GOURD);
        flammableBlockMaterials.add(Material.WEB);
    }
    protected static ItemStack burnItemInItemStack(ItemStack itemStack) {
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return itemStack;
        }

        String craftMaterial = InventoryUtil.getMaterialName(itemStack);
        if (flammableCraftMaterials.contains(craftMaterial)) {
            // TODO: Apparently a wooden hoe isn't made of wood... :/
            return new ItemStack(Items.ASH, itemStack.stackSize);
        }
        Item item = itemStack.getItem();
        if (item instanceof ItemBlock) {
            Block block = ((ItemBlock)item).getBlock();
            @SuppressWarnings("deprecation")
            Material blockMaterial = block.getMaterial(block.getDefaultState());
            if (flammableBlockMaterials.contains(blockMaterial)) {
                return new ItemStack(Items.ASH, itemStack.stackSize);
            }
        }
        if (item instanceof ItemFood) {
            return new ItemStack(Items.ASH, itemStack.stackSize);
        }
        for (int ore : OreDictionary.getOreIDs(itemStack)) {
            String oreName = OreDictionary.getOreName(ore);
            if (oreName.toLowerCase().contains("wood")) {
                return new ItemStack(Items.ASH, itemStack.stackSize);
            }
        }

        return itemStack;
    }

    protected static NamedItem IRON_INGOT = new NamedItem("iron_ingot");
    protected static NamedItem GOLD_INGOT = new NamedItem("gold_ingot");
    protected static Map<ResourceLocation, NamedItem> itemToMelted = new HashMap<>();
    static {
        itemToMelted.put(new ResourceLocation("bucket"), IRON_INGOT);
        itemToMelted.put(new ResourceLocation("shears"), IRON_INGOT);
        itemToMelted.put(new ResourceLocation(ModState.ADINFEROS_ID, "golden_bucket"), GOLD_INGOT);
        itemToMelted.put(new ResourceLocation(ModState.ADINFEROS_ID, "golden_shears"), GOLD_INGOT);
    }
    protected static ItemStack meltItemInItemStack(ItemStack itemStack) {
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return itemStack;
        }

        // Melt a tool/sword/armor/etc if its material is a metal ingot
        {
            ItemStack meltStack = InventoryUtil.getMaterialStack(itemStack);
            if (!InventoryUtil.isEmptyItemStack(meltStack)) {
                Item meltItem = meltStack.getItem();
                String registryString = meltItem.getRegistryName().toString();
                boolean isMeltable = registryString.contains("ingot") &&
                        !registryString.contains("obsidian") &&
                        !registryString.contains("netherite");
                if (isMeltable) {
                    // A single ingot is fine
                    return meltStack;
                }
            }
        }

        // Custom melting
        {
            Item item = itemStack.getItem();
            ResourceLocation itemResource = item.getRegistryName();
            if (itemToMelted.containsKey(itemResource)) {
                NamedItem meltName = itemToMelted.get(itemResource);
                Item meltItem = meltName.get();
                ItemStack meltStack = new ItemStack(meltItem);
                return meltStack;
            }
        }

        return itemStack;
    }

    /** Scatter some fire near the player */
    protected static void setWorldAflame(EntityPlayer player, Data data) {
        int minFlames = 3;
        int maxFlames = 12;
        int flames = data.random.nextInt(maxFlames - minFlames + 1) - minFlames;
        for (int i = 0; i < flames; ++i) {
            double randomRayX = 1.0 - (2.0 * data.random.nextDouble());
            double randomRayZ = 1.0 - (2.0 * data.random.nextDouble());
            // Bias toward negative y.
            double randomRayY = 1.0 - (2.0 * Math.pow(data.random.nextDouble(), 3.0));
            Vec3d randomRay = new Vec3d(randomRayX, randomRayY, randomRayZ);
            randomRay.normalize();
            Vec3d playerRayStart = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
            Vec3d playerRayEnd = playerRayStart.add(randomRay);
            RayTraceResult result = player.world.rayTraceBlocks(playerRayStart, playerRayEnd, true, true, false);
            if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                player.world.setBlockState(result.getBlockPos(), Blocks.FIRE.getDefaultState());
            }
        }
    }
    
    public void doOverheatEvent(EntityPlayer player, Data data, float amplifier) {
        // These are random and occur semi-frequently

        InventoryPlayer inventoryPlayer = player.inventory;
        int n = inventoryPlayer.getSizeInventory();
        float itemOverheatBaseChance = 0.25F * amplifier;
        float minItemOverheat = amplifier * 0.5F;
        float maxItemOverheat = amplifier;
        for (int i = 0; i < n; ++i) {
            if (data.random.nextFloat() >= itemOverheatBaseChance) {
                continue;
            }
            ItemStack itemStack = inventoryPlayer.getStackInSlot(i);
            if (InventoryUtil.isEmptyItemStack(itemStack)) {
                continue;
            }
            float overheatTemp = minItemOverheat + (data.random.nextFloat() * (maxItemOverheat - minItemOverheat));
            itemStack = meltIceInItemStack(itemStack);
            if (overheatTemp >= 0.3F) {
                itemStack = boilLiquidInItemStack(itemStack);
            }
            if (overheatTemp >= 1.0F) {
                itemStack = burnItemInItemStack(itemStack);
            }
            if (overheatTemp >= 1.8F) {
                itemStack = meltItemInItemStack(itemStack);
            }
            inventoryPlayer.setInventorySlotContents(i, itemStack);
        }

        if (amplifier >= 2.0F) {
            setWorldAflame(player, data);
        }

        player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS,
                0.5F, 2.6F + (data.random.nextFloat() - data.random.nextFloat()) * 0.8F);
    }

    @Override
    public void onActivate(EntityPlayer player, float amplifier) {
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        Data data = (Data)instinct.getInstinctEffectData(this);

        data.maxOverheatTimer = (int)(OVERHEAT_TIME_PER_AMPLIFIER * amplifier);
    }

    @Override
    public void onDeactivate(EntityPlayer player, float amplifier) {
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return;
        }
        Data data = (Data)instinct.getInstinctEffectData(this);
        data.overheatTimer = 0;
        data.nextOverheatEventTime = 0;
    }

    /** See also ListenerInstinctOverheat */
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
            exposeToHeat(player, data);
        }
        if (data.overheatTimer > 0) {
            if (data.nextOverheatEventTime > OVERHEAT_EVENT_FREQUENCY &&
                    data.random.nextFloat() >= OVERHEAT_EVENT_CHANCE) {
                doOverheatEvent(player, data, amplifier);
                data.nextOverheatEventTime = 0;
            }
        }
        data.updateTimers();
    }

}
