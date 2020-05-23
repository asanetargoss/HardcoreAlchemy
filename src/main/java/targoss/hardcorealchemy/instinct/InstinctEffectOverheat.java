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

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.instinct.api.IInstinctEffectData;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;
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

    protected static class Data implements IInstinctEffectData {
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
    }
    
    @Override
    public IInstinctEffectData createData() {
        return new Data();
    }

    public void exposeToHeat(EntityPlayer player, Data data) {
        assert(!player.world.isRemote);
        if (!player.world.isRemote) {
            Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.effect.overheat.exposed"));
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
    
    protected static ItemStack meltIceInItemStack(ItemStack itemStack) {
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return itemStack;
        }

        // TODO
        return itemStack;
    }

    protected static ItemStack burnItemInItemStack(ItemStack itemStack) {
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return itemStack;
        }

        // TODO: ItemSword flammable by material
        // TODO: ItemTool flammable by material
        // TODO: Block flammable by material
        // TODO: For the rest, look up in a map (sticks, food, etc)
        // TODO: If aflame then return ash (new item)
        return itemStack;
    }

    protected static Map<ResourceLocation, NamedItem> itemToMelted = new HashMap<>();
    protected static NamedItem IRON_INGOT = new NamedItem("iron_ingot");
    protected static NamedItem GOLD_INGOT = new NamedItem("gold_ingot");
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

        // Melt a tool/sword/armor/etc if its material is an ingot
        {
            ItemStack meltStack = InventoryUtil.getMaterialStack(itemStack);
            if (!InventoryUtil.isEmptyItemStack(meltStack)) {
                Item meltItem = meltStack.getItem();
                boolean isMeltable = meltItem.getRegistryName().toString().contains("ingot");
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

    protected static void setWorldAflame(EntityPlayer player) {
        // TODO
        // n times
        // Get random pos
        // Touching block and exposed to air? -> Make fire
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
            setWorldAflame(player);
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
            // See also ListenerInstinctOverheat
        }
        data.updateTimers();
    }

}
