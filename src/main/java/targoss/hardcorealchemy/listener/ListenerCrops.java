/*
 * Copyright 2017-2018 asanetargoss
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

package targoss.hardcorealchemy.listener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.pam.harvestcraft.blocks.CropRegistry;
import com.pam.harvestcraft.blocks.growables.BlockPamCrop;
import com.pam.harvestcraft.config.ConfigHandler;
import com.pam.harvestcraft.item.ItemRegistry;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.oredict.OreDictionary;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.util.InventoryUtil;
import toughasnails.api.TANBlocks;
import toughasnails.api.config.GameplayOption;
import toughasnails.api.config.SyncedConfig;
import toughasnails.api.season.Season;
import toughasnails.api.season.SeasonHelper;
import toughasnails.api.temperature.Temperature;
import toughasnails.api.temperature.TemperatureHelper;

public class ListenerCrops extends ConfiguredListener {
    public ListenerCrops(Configs configs) {
        super(configs);
    }

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
        if (!ModState.isTanLoaded ||
                !isWinterAt(world, pos)) {
            return false;
        }
        return true;
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
