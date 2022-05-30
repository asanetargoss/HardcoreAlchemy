/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Survival.
 *
 * Hardcore Alchemy Survival is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Survival is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Survival. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.survival.listener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.pam.harvestcraft.blocks.CropRegistry;
import com.pam.harvestcraft.blocks.growables.BlockPamCrop;
import com.pam.harvestcraft.config.ConfigHandler;
import com.pam.harvestcraft.item.ItemRegistry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockReed;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.oredict.OreDictionary;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.InventoryUtil;
import toughasnails.api.TANBlocks;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.season.Season;
import toughasnails.api.season.SeasonHelper;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureHelper;

public class ListenerCrops extends HardcoreAlchemyListener {
    private static Set<String> hibernators;
    
    static {
        hibernators = new HashSet<>();
        hibernators.add("com.pam.harvestcraft.blocks.growables.BlockPamFruit");
        hibernators.add("com.pam.harvestcraft.blocks.growables.BlockPamFruitLog");
    }
    
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        if (ModState.isHarvestCraftLoaded) {
            fixPamSeeds();
        }
    }
    
    /**
     * Fixes broken config option for Pam's Harvestcraft,
     * so the mod's crops can drop seeds when immature.
     * Called in preInit, so it's before most mod intercompatibility code
     */
    @Optional.Method(modid = ModState.HARVESTCRAFT_ID)
    public void fixPamSeeds() {
        if (!ConfigHandler.cropsdropSeeds) {
            return;
        }
        
        Map<String, BlockPamCrop> allCrops = CropRegistry.getCrops();
        Map<String, Item> allSeeds = CropRegistry.getSeeds();
        
        try {
            Method methodGetSeedName = CropRegistry.class.getDeclaredMethod("getSeedName", String.class);
            methodGetSeedName.setAccessible(true);
            
            Field fieldSeed = BlockPamCrop.class.getDeclaredField("seed");
            fieldSeed.setAccessible(true);
            
            for (Map.Entry<String, BlockPamCrop> cropEntry : allCrops.entrySet()) {
                String cropName = cropEntry.getKey();
                
                BlockPamCrop cropBlock = cropEntry.getValue();
                String actualSeedName = (String)methodGetSeedName.invoke(null, cropName);
                Item actualSeed = ItemRegistry.items.get(actualSeedName);
                
                fieldSeed.set(cropBlock, actualSeed);
                
                allSeeds.put(cropName, actualSeed);
                
            }
        }
        catch (Exception e) {
            HardcoreAlchemy.LOGGER.error("Failed to modify Harvestcraft to make crops drop seeds");
            e.printStackTrace();
        }
    }
    
    @Optional.Method(modid=ModState.TAN_ID)
    private static boolean isWinterAt(World world, BlockPos pos) {
        Season season = SeasonHelper.getSeasonData(world).getSubSeason().getSeason();
        if (season != Season.WINTER ||
                TemperatureHelper.isPosClimatisedForTemp(world, pos, new Temperature(1)) ||
                !SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)
                ) {
            return false;
        }
        
        return true;
    }

    /**
     * Faster alternative to isWinterAt which ignores
     * temperature effects of nearby blocks
     */
    @Optional.Method(modid=ModState.TAN_ID)
    private static boolean isWinterAtFast(World world) {
        Season season = SeasonHelper.getSeasonData(world).getSubSeason().getSeason();
        if (season != Season.WINTER ||
                !SyncedConfig.getBooleanValue(GameplayOption.ENABLE_SEASONS)
                ) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Coremod hook. See TDecayingCrops
     */
    @CoremodHook
    public static void checkDecay(IBlockState blockState, World world, BlockPos pos) {
        if (ModState.isTanLoaded && isWinterAt(world, pos)) {
            world.setBlockState(pos, TANBlocks.dead_crops.getDefaultState());
        }
    }
    
    /**
     * Coremod hook. See THibernatingCrops
     */
    @CoremodHook
    public static boolean shouldHibernate(IBlockState blockState, World world, BlockPos pos) {
        // None of these plants should grow in Winter
        if (ModState.isTanLoaded &&
                isWinterAt(world, pos)) {
            return true;
        }
        // Reeds should not grow if they are not next to water
        Block block = blockState.getBlock();
        if (block instanceof BlockReed) {
            BlockPos posBelow = pos;
            Block blockBelow = block;
            for (int i = 0; i < 3; i++) {
                posBelow = pos.down();
                blockBelow = world.getBlockState(posBelow).getBlock();
                if (!(blockBelow instanceof BlockReed)) {
                    if (world.getBlockState(posBelow.east()).getMaterial() != Material.WATER &&
                            world.getBlockState(posBelow.west()).getMaterial() != Material.WATER &&
                            world.getBlockState(posBelow.north()).getMaterial() != Material.WATER &&
                            world.getBlockState(posBelow.south()).getMaterial() != Material.WATER) {
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }
    
    /**
     * Coremod hook. See TBlockReed
     * 
     * No reed stacking allowed in winter unless the bottom reed is adjacent to water
     */
    @CoremodHook
    public static boolean canReedStay(Block block, World world, BlockPos pos) {
        boolean isWinter = ModState.isTanLoaded && isWinterAtFast(world);
        if (isWinter) {
            BlockPos belowPos = pos;
            for (int i = 0; i < 3; ++i) {
                belowPos = belowPos.down();
                IBlockState belowState = world.getBlockState(belowPos);
                Block below = belowState.getBlock();
                if (!(below instanceof BlockReed)) {
                    if (i == 0) {
                        // Yield logic to vanilla
                        break;
                    }
                    else if (world.getBlockState(belowPos.east()).getMaterial() != Material.WATER &&
                            world.getBlockState(belowPos.west()).getMaterial() != Material.WATER &&
                            world.getBlockState(belowPos.north()).getMaterial() != Material.WATER &&
                            world.getBlockState(belowPos.south()).getMaterial() != Material.WATER) {
                        return false;
                    }
                    else {
                        // Yield logic to vanilla
                        break;
                    }
                }
            }
        }
        return block.canPlaceBlockAt(world, pos);
    }
    
    /**
     * Coremod hook. See TBlock
     */
    @CoremodHook
    public static boolean hasWaterAlternative(IBlockAccess world, BlockPos pos) {
        boolean hasIce = world.getBlockState(pos.east()).getMaterial() == Material.ICE ||
                world.getBlockState(pos.west()).getMaterial() == Material.ICE ||
                world.getBlockState(pos.north()).getMaterial() == Material.ICE ||
                world.getBlockState(pos.south()).getMaterial() == Material.ICE;
        return hasIce;
    }
    
    /**
     * Coremod hook. See TRightClickHarvesting
     * Returns true unless this is a player holding a stick in their main hand
     */
    @CoremodHook
    public static boolean allowRightClickHarvest(RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null) {
            return true;
        }
        
        if (isStick(player.getHeldItemMainhand()) ||
                isStick(player.getHeldItemOffhand())) {
            return false;
        }
        
        return true;
    }
    
    public static boolean isStick(ItemStack stack) {
        if (InventoryUtil.isEmptyItemStack(stack)) {
            return false;
        }
        
        // Is the player holding a stick? (Check the ore dictionary)
        int[] ores = OreDictionary.getOreIDs(stack);
        int stick = OreDictionary.getOreID("stickWood");
        for (int ore : ores) {
            if (ore == stick) {
                return true;
            }
        }
        
        return false;
    }
}
