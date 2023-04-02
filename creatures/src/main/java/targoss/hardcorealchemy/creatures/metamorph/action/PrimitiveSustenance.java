/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures.metamorph.action;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import mchorse.metamorph.api.abilities.IAction;
import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.FoodStats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.INutritionExtension;
import targoss.hardcorealchemy.util.MiscVanilla;
import targoss.hardcorealchemy.util.MorphDiet;
import targoss.hardcorealchemy.util.NutritionExtension;

public class PrimitiveSustenance implements IAction {
    
    protected static final int FOOD_SUSTAIN = 4;
    protected static final int FOOD_SATURATION_SUSTAIN = 4;
    protected static final int THIRST_SUSTAIN = 4;
    protected static final int THIRST_SATURATION_SUSTAIN = 4;

    protected final ITextComponent DISABLED_MESSAGE = new TextComponentTranslation("hardcorealchemy.ability.sustenance.disabled");
    protected final ITextComponent INVALID_MESSAGE = new TextComponentTranslation("hardcorealchemy.ability.sustenance.invalid");
    protected final ITextComponent NOT_AVAILABLE_GRASS = new TextComponentTranslation("hardcorealchemy.ability.sustenance.not_available.grass");
    protected final ITextComponent NOT_AVAILABLE_DRINK = new TextComponentTranslation("hardcorealchemy.ability.sustenance.not_available.drink");
    protected final ITextComponent NOT_AVAILABLE_GRASS_DRINK = new TextComponentTranslation("hardcorealchemy.ability.sustenance.not_available.grass_drink");
    protected final ITextComponent NOT_READY_GRASS = new TextComponentTranslation("hardcorealchemy.ability.sustenance.not_ready.grass");
    protected final ITextComponent NOT_READY_DRINK = new TextComponentTranslation("hardcorealchemy.ability.sustenance.not_ready.drink");
    
    protected static class SustenanceCondition {
        protected int grassNutritionExtensionSuccess;
        protected int drinkNutritionExtensionSuccess;
        protected ITextComponent response;

        public SustenanceCondition(int grassNutritionExtensionSuccess, int drinkNutritionExtensionSuccess, ITextComponent response) {
            this.grassNutritionExtensionSuccess = grassNutritionExtensionSuccess;
            this.drinkNutritionExtensionSuccess = drinkNutritionExtensionSuccess;
            this.response = response;
        }
        
        public boolean matches(int grassNutritionExtensionSuccess, int drinkNutritionExtensionSuccess) {
            return (this.grassNutritionExtensionSuccess & grassNutritionExtensionSuccess) != 0 && (this.drinkNutritionExtensionSuccess & drinkNutritionExtensionSuccess) != 0;
        }
        
        public ITextComponent getResponse() {
            return response;
        }
    }
    
    protected final SustenanceCondition[] sustenanceConditions = {
        new SustenanceCondition(INutritionExtension.Success.NOT_AVAILABLE, INutritionExtension.Success.NONE, NOT_AVAILABLE_GRASS),
        new SustenanceCondition(INutritionExtension.Success.NONE, INutritionExtension.Success.NOT_AVAILABLE, NOT_AVAILABLE_DRINK),
        new SustenanceCondition(INutritionExtension.Success.NOT_AVAILABLE, INutritionExtension.Success.NOT_AVAILABLE, NOT_AVAILABLE_GRASS_DRINK),
        new SustenanceCondition(INutritionExtension.Success.NOT_READY, INutritionExtension.Success.NOT_AVAILABLE | INutritionExtension.Success.NONE, NOT_READY_GRASS),
        new SustenanceCondition(INutritionExtension.Success.NOT_AVAILABLE | INutritionExtension.Success.NONE, INutritionExtension.Success.NOT_READY, NOT_READY_DRINK)
    };
    
    protected final ITextComponent getSustenanceResponse(int grassNutritionExtensionSuccess, int drinkNutritionExtensionSuccess) {
        for (SustenanceCondition condition : sustenanceConditions) {
            if (condition.matches(grassNutritionExtensionSuccess, drinkNutritionExtensionSuccess)) {
                return condition.getResponse();
            }
        }
        return INVALID_MESSAGE;
    }
    
    @Override
    public void execute(EntityLivingBase entity, AbstractMorph morph) {
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer)entity;
        
        MorphDiet.Needs needs = NutritionExtension.INSTANCE.getNeeds(player);
        if (needs == MorphDiet.PLAYER_NEEDS) {
            if (!player.world.isRemote) {
                Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, DISABLED_MESSAGE);
            }
            return;
        }
        
        Vec3d eyeStartPos = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3d lookDir = player.getLook(1.0f);
        float reachDistance = MiscVanilla.getPlayerReachDistance(player);
        Vec3d endPos = eyeStartPos.addVector(lookDir.xCoord * reachDistance, lookDir.yCoord * reachDistance, lookDir.zCoord * reachDistance);
        RayTraceResult lookingAt = player.world.rayTraceBlocks(eyeStartPos, endPos, true, false, true);
        BlockPos lookPos = lookingAt.getBlockPos();
        IBlockState blockState = player.world.getBlockState(lookPos);
        
        int grassNutritionExtensionSuccess = eatGrass(player, needs, lookPos, blockState);
        if (grassNutritionExtensionSuccess == INutritionExtension.Success.SUCCESS) {
            return;
        }
        
        int thirstNutritionExtensionSuccess = INutritionExtension.Success.NONE;
        if (ModState.isTanLoaded) {
            thirstNutritionExtensionSuccess = NutritionExtension.INSTANCE.drinkWater(player, needs, lookPos, blockState, THIRST_SUSTAIN, 0);
            if (thirstNutritionExtensionSuccess == INutritionExtension.Success.SUCCESS) {
                return;
            }
        }
        
        if (grassNutritionExtensionSuccess == INutritionExtension.Success.NONE && thirstNutritionExtensionSuccess == INutritionExtension.Success.NONE) {
            // This ability is doing nothing for this morph due to external configuration.
            return;
        }
        
        // The player can't eat and/or drink here. Display the relevant message.
        if (!player.world.isRemote) {
            ITextComponent response = getSustenanceResponse(grassNutritionExtensionSuccess, thirstNutritionExtensionSuccess);
            Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, response);
        }
    }
    
    protected static final Predicate<IBlockState> IS_TALL_GRASS = BlockStateMatcher.forBlock(Blocks.TALLGRASS).where(BlockTallGrass.TYPE, Predicates.equalTo(BlockTallGrass.EnumType.GRASS));
    protected static final Predicate<IBlockState> IS_DOUBLE_TALL_GRASS = BlockStateMatcher.forBlock(Blocks.DOUBLE_PLANT).where(BlockDoublePlant.VARIANT, Predicates.equalTo(BlockDoublePlant.EnumPlantType.SUNFLOWER));
    
    protected int eatGrass(EntityPlayer player, MorphDiet.Needs needs, BlockPos pos, IBlockState blockState) {
        if (needs.nutrients.length != 1 || !needs.containsNutrient("grain")) {
            return INutritionExtension.Success.NONE;
        }
        
        FoodStats foodStats = player.getFoodStats();
        
        if (IS_TALL_GRASS.apply(blockState) || IS_DOUBLE_TALL_GRASS.apply(blockState)) {
            if (foodStats.getFoodLevel() >= FOOD_SUSTAIN) {
                return INutritionExtension.Success.NOT_READY;
            }
            
            if (!player.world.isRemote) {
                player.world.destroyBlock(pos, false);
            }
            restoreHunger(player, foodStats);
            NutritionExtension.INSTANCE.restoreThirst(player, needs, THIRST_SUSTAIN, THIRST_SATURATION_SUSTAIN);
            NutritionExtension.INSTANCE.addGrassToFoodHistory(player);
            return INutritionExtension.Success.SUCCESS;
        }
        
        if (blockState.getBlock() instanceof BlockGrass) {
            if (foodStats.getFoodLevel() >= FOOD_SUSTAIN) {
                return INutritionExtension.Success.NOT_READY;
            }
            
            if (!player.world.isRemote) {
                player.world.playEvent(2001, pos, Block.getIdFromBlock(Blocks.GRASS));
                player.world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 2);
            }
            restoreHunger(player, foodStats);
            NutritionExtension.INSTANCE.restoreThirst(player, needs, THIRST_SUSTAIN, THIRST_SATURATION_SUSTAIN);
            NutritionExtension.INSTANCE.addGrassToFoodHistory(player);
            return INutritionExtension.Success.SUCCESS;
        }
        
        return INutritionExtension.Success.NOT_AVAILABLE;
    }
    
    protected void restoreHunger(EntityPlayer player, FoodStats foodStats) {
        foodStats.setFoodLevel(Math.min(foodStats.getFoodLevel() + 2, FOOD_SUSTAIN));
        foodStats.addStats(0, FOOD_SATURATION_SUSTAIN);
        NutritionExtension.INSTANCE.restoreNutrient(player, "grain", 0.5f);
    }
}
