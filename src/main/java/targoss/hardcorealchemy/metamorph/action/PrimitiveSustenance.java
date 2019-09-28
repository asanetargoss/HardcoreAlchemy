/*
 * Copyright 2019 asanetargoss
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

package targoss.hardcorealchemy.metamorph.action;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import mchorse.metamorph.api.abilities.IAction;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.EntityMorph;
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
import net.minecraft.init.SoundEvents;
import net.minecraft.util.FoodStats;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Optional;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ProviderHumanity;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.MiscVanilla;
import targoss.hardcorealchemy.util.MorphDiet;
import toughasnails.api.stat.capability.IThirst;
import toughasnails.api.thirst.ThirstHelper;

public class PrimitiveSustenance implements IAction {
    public static class Success {
        public static final int NONE = 1 << 0;
        public static final int SUCCESS = 1 << 1;
        public static final int NOT_AVAILABLE = 1 << 2;
        public static final int NOT_READY = 1 << 3;
    }
    
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
        protected int grassSuccess;
        protected int drinkSuccess;
        protected ITextComponent response;

        public SustenanceCondition(int grassSuccess, int drinkSuccess, ITextComponent response) {
            this.grassSuccess = grassSuccess;
            this.drinkSuccess = drinkSuccess;
            this.response = response;
        }
        
        public boolean matches(int grassSuccess, int drinkSuccess) {
            return (this.grassSuccess & grassSuccess) != 0 && (this.drinkSuccess & drinkSuccess) != 0;
        }
        
        public ITextComponent getResponse() {
            return response;
        }
    }
    
    protected final SustenanceCondition[] sustenanceConditions = {
        new SustenanceCondition(Success.NOT_AVAILABLE, Success.NONE, NOT_AVAILABLE_GRASS),
        new SustenanceCondition(Success.NONE, Success.NOT_AVAILABLE, NOT_AVAILABLE_DRINK),
        new SustenanceCondition(Success.NOT_AVAILABLE, Success.NOT_AVAILABLE, NOT_AVAILABLE_GRASS_DRINK),
        new SustenanceCondition(Success.NOT_READY, Success.NOT_AVAILABLE | Success.NONE, NOT_READY_GRASS),
        new SustenanceCondition(Success.NOT_AVAILABLE | Success.NONE, Success.NOT_READY, NOT_READY_DRINK)
    };
    
    protected final ITextComponent getSustenanceResponse(int grassSuccess, int drinkSuccess) {
        for (SustenanceCondition condition : sustenanceConditions) {
            if (condition.matches(grassSuccess, drinkSuccess)) {
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
        
        ICapabilityHumanity humanity = player.getCapability(ProviderHumanity.HUMANITY_CAPABILITY, null);
        if (humanity == null || humanity.getHumanity() > 0.0D) {
            if (!player.world.isRemote) {
                Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, DISABLED_MESSAGE);
            }
            return;
        }
        
        if (!(morph instanceof EntityMorph)) {
            return;
        }
        MorphDiet.Needs needs = MorphDiet.getNeeds((EntityMorph)morph);
        
        Vec3d eyeStartPos = new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3d lookDir = player.getLook(1.0f);
        float reachDistance = MiscVanilla.getPlayerReachDistance(player);
        Vec3d endPos = eyeStartPos.addVector(lookDir.xCoord * reachDistance, lookDir.yCoord * reachDistance, lookDir.zCoord * reachDistance);
        RayTraceResult lookingAt = player.world.rayTraceBlocks(eyeStartPos, endPos, true, false, true);
        BlockPos lookPos = lookingAt.getBlockPos();
        IBlockState blockState = player.world.getBlockState(lookPos);
        
        int grassSuccess = eatGrass(player, needs, lookPos, blockState);
        if (grassSuccess == Success.SUCCESS) {
            return;
        }
        
        int thirstSuccess = Success.NONE;
        if (ModState.isTanLoaded) {
            thirstSuccess = drinkWater(player, needs, lookPos, blockState);
            if (thirstSuccess == Success.SUCCESS) {
                return;
            }
        }
        
        if (grassSuccess == Success.NONE && thirstSuccess == Success.NONE) {
            // This ability is doing nothing for this morph due to external configuration.
            return;
        }
        
        // The player can't eat and/or drink here. Display the relevant message.
        if (!player.world.isRemote) {
            ITextComponent response = getSustenanceResponse(grassSuccess, thirstSuccess);
            Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, response);
        }
    }
    
    protected static final Predicate<IBlockState> IS_TALL_GRASS = BlockStateMatcher.forBlock(Blocks.TALLGRASS).where(BlockTallGrass.TYPE, Predicates.equalTo(BlockTallGrass.EnumType.GRASS));
    protected static final Predicate<IBlockState> IS_DOUBLE_TALL_GRASS = BlockStateMatcher.forBlock(Blocks.DOUBLE_PLANT).where(BlockDoublePlant.VARIANT, Predicates.equalTo(BlockDoublePlant.EnumPlantType.SUNFLOWER));
    
    protected int eatGrass(EntityPlayer player, MorphDiet.Needs needs, BlockPos pos, IBlockState blockState) {
        if (needs.nutrients.length != 1 || !needs.containsNutrient("grain")) {
            return Success.NONE;
        }
        
        FoodStats foodStats = player.getFoodStats();
        
        if (IS_TALL_GRASS.apply(blockState) || IS_DOUBLE_TALL_GRASS.apply(blockState)) {
            if (foodStats.getFoodLevel() >= FOOD_SUSTAIN) {
                return Success.NOT_READY;
            }
            
            if (!player.world.isRemote) {
                player.world.destroyBlock(pos, false);
            }
            foodStats.setFoodLevel(Math.min(foodStats.getFoodLevel() + 2, FOOD_SUSTAIN));
            foodStats.addStats(0, FOOD_SATURATION_SUSTAIN);
            return Success.SUCCESS;
        }
        
        if (blockState.getBlock() instanceof BlockGrass) {
            if (foodStats.getFoodLevel() >= FOOD_SUSTAIN) {
                return Success.NOT_READY;
            }
            
            if (!player.world.isRemote) {
                player.world.playEvent(2001, pos, Block.getIdFromBlock(Blocks.GRASS));
                player.world.setBlockState(pos, Blocks.DIRT.getDefaultState(), 2);
            }
            foodStats.setFoodLevel(Math.min(foodStats.getFoodLevel() + 2, FOOD_SUSTAIN));
            foodStats.addStats(0, FOOD_SATURATION_SUSTAIN);
            return Success.SUCCESS;
        }
        
        return Success.NOT_AVAILABLE;
    }
    
    @Optional.Method(modid = ModState.TAN_ID)
    protected int drinkWater(EntityPlayer player, MorphDiet.Needs needs, BlockPos pos, IBlockState blockState) {
        if (!needs.hasThirst) {
            return Success.NONE;
        }
        
        Block block = blockState.getBlock();
        if (block == Blocks.WATER || block == Blocks.FLOWING_WATER) {
            IThirst thirst = ThirstHelper.getThirstData(player);
            if (thirst.getThirst() >= THIRST_SUSTAIN) {
                return Success.NOT_READY;
            }
            thirst.setThirst(Math.min(THIRST_SUSTAIN, thirst.getThirst() + 2));
            thirst.setHydration(Math.min(THIRST_SATURATION_SUSTAIN, thirst.getHydration()));
            if (!player.world.isRemote) {
                player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.PLAYERS, 0.5F, player.world.rand.nextFloat() * 0.1F + 0.9F);
            }
            
            return Success.SUCCESS;
        }
        
        return Success.NOT_AVAILABLE;
    }
}
