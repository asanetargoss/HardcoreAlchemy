/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

public class Interaction {
    /**
     * Whether a block has a special right-click use
     */
    public static boolean hasSpecialUse(Block block) {
        if (block instanceof BlockCrops) {
            return false;
        }
        
        return hasUseFunction(block);
    }
    
    /**
     * Whether an item has a special right-click use
     */
    public static boolean hasSpecialUse(Item item) {
        if (item instanceof ItemBlock) {
            return false;
        }
        if (item instanceof ItemFood) {
            return false;
        }
        
        return hasUseFunction(item);
    }
    
    private static final ObfuscatedName ON_ITEM_RIGHT_CLICK = new ObfuscatedName("func_77659_a" /*onItemRightClick*/);
    private static final ObfuscatedName ON_ITEM_USE = new ObfuscatedName("func_180614_a" /*onItemUse*/);
    private static final ObfuscatedName ON_ITEM_USE_FINISH = new ObfuscatedName("func_77654_b" /*onItemUseFinish*/);
    private static final ObfuscatedName ON_ITEM_USE_FIRST = new ObfuscatedName("onItemUseFirst" /*Forge method*/);
    private static final ObfuscatedName ON_LEFT_CLICK_ENTITY = new ObfuscatedName("onLeftClickEntity" /*Forge method*/);
    private static final ObfuscatedName ON_PLAYER_STOPPED_USING = new ObfuscatedName("onPlayerStoppedUsing" /*Forge method*/);
    
    private static Map<Item, Boolean> itemsHaveUse = new ConcurrentHashMap<Item, Boolean>();
    
    /**
     * Checks for any item use (right click) function
     * overridden from the base Item class.
     */
    public static boolean hasUseFunction(Item item) {
        Boolean hasUse = itemsHaveUse.get(item);
        if (hasUse != null) {
            return hasUse;
        }
        hasUse = hasUseFunctionImpl(item);
        itemsHaveUse.put(item, hasUse);
        return hasUse;
    }
    
    private static boolean hasUseFunctionImpl(Item item) {
        if (InvokeUtil.hasPrivateMethod(false, item.getClass(), Item.class,
                ON_ITEM_RIGHT_CLICK.get(),
                ItemStack.class, World.class, EntityPlayer.class, EnumHand.class)) {
            return true;
        }
        if (InvokeUtil.hasPrivateMethod(false, item.getClass(), Item.class,
                ON_ITEM_USE.get(),
                ItemStack.class, EntityPlayer.class, World.class,
                BlockPos.class, EnumHand.class, EnumFacing.class,
                float.class, float.class, float.class)) {
            return true;
        }
        if (InvokeUtil.hasPrivateMethod(false, item.getClass(), Item.class,
                ON_ITEM_USE_FINISH.get(),
                ItemStack.class, EntityPlayer.class, World.class,
                BlockPos.class, EnumHand.class, EnumFacing.class,
                float.class, float.class, float.class)) {
            return true;
        }
        if (InvokeUtil.hasPrivateMethod(false, item.getClass(), Item.class,
                ON_ITEM_USE_FIRST.get(),
                ItemStack.class, EntityPlayer.class, World.class,
                BlockPos.class, EnumFacing.class,
                float.class, float.class, float.class,
                EnumHand.class)) {
            return true;
        }
        if (InvokeUtil.hasPrivateMethod(false, item.getClass(), Item.class,
                ON_LEFT_CLICK_ENTITY.get(),
                ItemStack.class, EntityPlayer.class, Entity.class)) {
            return true;
        }
        if (InvokeUtil.hasPrivateMethod(false, item.getClass(), Item.class,
                ON_PLAYER_STOPPED_USING.get(),
                ItemStack.class, World.class, EntityLivingBase.class, int.class)) {
            return true;
        }
        
        return false;
    }
    
    private static final ObfuscatedName ON_BLOCK_ACTIVATED = new ObfuscatedName("func_180639_a" /*onBlockActivated*/);
    private static final ObfuscatedName ON_BLOCK_CLICKED = new ObfuscatedName("func_180649_a" /*onBlockClicked*/);
    
    private static Map<Block, Boolean> blocksHaveUse = new ConcurrentHashMap<Block, Boolean>();
    
    /**
     * Checks for any block use (right click) function
     * overridden from the base Block class.
     */
    public static boolean hasUseFunction(Block block) {
        Boolean hasUse = blocksHaveUse.get(block);
        if (hasUse != null) {
            return hasUse;
        }
        hasUse = hasUseFunctionImpl(block);
        blocksHaveUse.put(block, hasUse);
        return hasUse;
    }
    
    private static boolean hasUseFunctionImpl(Block block) {
        if (InvokeUtil.hasPrivateMethod(false, block.getClass(), Block.class,
                ON_BLOCK_ACTIVATED.get(),
                World.class, BlockPos.class, IBlockState.class,
                EntityPlayer.class, EnumHand.class, ItemStack.class,
                EnumFacing.class,
                float.class, float.class, float.class)) {
            return true;
        }
        if (InvokeUtil.hasPrivateMethod(false, block.getClass(), Block.class,
                ON_BLOCK_CLICKED.get(),
                World.class, BlockPos.class, EntityPlayer.class)) {
            return true;
        }
        return false;
    }
}
