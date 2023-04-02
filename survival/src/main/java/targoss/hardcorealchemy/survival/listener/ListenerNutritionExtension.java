/*
 * Copyright 2017-2023 asanetargoss
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

import ca.wescook.nutrition.capabilities.CapInterface;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.nutrients.NutrientList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import squeek.spiceoflife.foodtracker.FoodEaten;
import squeek.spiceoflife.foodtracker.FoodHistory;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.INutritionExtension;
import targoss.hardcorealchemy.util.MorphDiet;
import targoss.hardcorealchemy.util.MorphDiet.Needs;
import targoss.hardcorealchemy.util.NutritionExtension;
import toughasnails.api.stat.capability.IThirst;
import toughasnails.api.thirst.IDrink;
import toughasnails.api.thirst.ThirstHelper;

public class ListenerNutritionExtension extends HardcoreAlchemyListener {
    public static class Wrapper implements INutritionExtension {
        public INutritionExtension delegate;

        public Wrapper(INutritionExtension delegate) {
            this.delegate = delegate;
        }
        
        @CapabilityInject(CapInterface.class)
        public static final Capability<CapInterface> NUTRITION_CAPABILITY = null;
        
        public static class Nutrition {
            public static void restoreNutrient(EntityPlayer player, String nutrientName, float amount) {
                CapInterface nutrition = player.getCapability(NUTRITION_CAPABILITY, null);
                if (nutrition == null) {
                    return;
                }
                
                Nutrient nutrient = NutrientList.getByName(nutrientName);
                if (nutrient == null) {
                    return;
                }
                
                nutrition.add(nutrient, amount, true);
            }
        }
        
        public static class SpiceOfLife {
            public static void addGrassToFoodHistory(EntityPlayer player) {
                FoodHistory foodHistory = FoodHistory.get(player);
                if (foodHistory == null) {
                    return;
                }
                ItemStack grassStack = new ItemStack(Item.getItemFromBlock(Blocks.GRASS));
                foodHistory.addFood(new FoodEaten(grassStack, player));
            }
        }
        
        public static class ToughAsNails {
            public static void restoreThirst(EntityPlayer player, MorphDiet.Needs needs, int thirstSustain, int thirstSaturationSustain) {
                if (!needs.hasThirst) {
                    return;
                }
                
                IThirst thirst = ThirstHelper.getThirstData(player);
                if (thirst.getThirst() >= thirstSustain) {
                    return;
                }
                
                thirst.setThirst(Math.min(thirstSustain, thirst.getThirst() + 2));
                thirst.setHydration(Math.min(thirstSaturationSustain, thirst.getHydration() + thirstSaturationSustain));
            }
            
            public static int drinkWater(EntityPlayer player, MorphDiet.Needs needs, BlockPos pos, IBlockState blockState, int thirstSustain) {
                if (!needs.hasThirst) {
                    return INutritionExtension.Success.NONE;
                }
                
                Block block = blockState.getBlock();
                if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
                    IThirst thirst = ThirstHelper.getThirstData(player);
                    if (thirst.getThirst() >= thirstSustain) {
                        return INutritionExtension.Success.NOT_READY;
                    }
                    restoreThirst(player, needs, thirstSustain, 0);
                    if (!player.world.isRemote) {
                        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 0.5F, player.world.rand.nextFloat() * 0.1F + 0.9F);
                    }
                    
                    return INutritionExtension.Success.SUCCESS;
                }
                
                return INutritionExtension.Success.NOT_AVAILABLE;
            }

            public static boolean isItemDrinkable(Item item) {
                return item instanceof IDrink ||
                        item.getRegistryName().getResourcePath().equals("canteen");
            }
        }
        
        @Override
        public void restoreNutrient(EntityPlayer player, String nutrientName, float amount) {
            delegate.restoreNutrient(player, nutrientName, amount);
            if (ModState.isNutritionLoaded) {
                Nutrition.restoreNutrient(player, nutrientName, amount);
            }
        }

        @Override
        public void addGrassToFoodHistory(EntityPlayer player) {
            delegate.addGrassToFoodHistory(player);
            if (ModState.isSpiceOfLifeLoaded) {
                SpiceOfLife.addGrassToFoodHistory(player);
            }
        }

        @Override
        public void restoreThirst(EntityPlayer player, MorphDiet.Needs needs, int thirstSustain, int thirstSaturationSustain) {
            delegate.restoreThirst(player, needs, thirstSustain, thirstSaturationSustain);
            if (ModState.isTanLoaded) {
                ToughAsNails.restoreThirst(player, needs, thirstSustain, thirstSaturationSustain);
            }
        }

        @Override
        public int drinkWater(EntityPlayer player, MorphDiet.Needs needs, BlockPos pos, IBlockState blockState, int thirstSustain, int thirstSaturationSustain) {
            int success = delegate.drinkWater(player, needs, pos, blockState, thirstSustain, thirstSaturationSustain);
            if (success == INutritionExtension.Success.SUCCESS) {
                return INutritionExtension.Success.SUCCESS;
            }
            if (ModState.isTanLoaded) {
                success &= ToughAsNails.drinkWater(player, needs, pos, blockState, thirstSustain);
            }
            return INutritionExtension.Success.getSuccessValue(success);
        }
        
        @Override
        public boolean isItemDrinkable(Item item) {
            if (delegate.isItemDrinkable(item)) {
                return true;
            }
            if (ModState.isTanLoaded && ToughAsNails.isItemDrinkable(item)) {
                return true;
            }
            return false;
        }

        @Override
        public Needs getNeeds(EntityPlayer player) {
            return delegate.getNeeds(player);
        }
    }
    
    public void preInit(FMLPreInitializationEvent event) {
        NutritionExtension.INSTANCE = new Wrapper(NutritionExtension.INSTANCE);
    }
}
