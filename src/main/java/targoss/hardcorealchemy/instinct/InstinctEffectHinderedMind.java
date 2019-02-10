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

package targoss.hardcorealchemy.instinct;

import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.EntityMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.instinct.api.InstinctEffect;
import targoss.hardcorealchemy.util.InventoryUtil;

/**
 * Most behavior is handled through ListenerPlayerHinderedMind
 * Amplitude breakdown:
 * 0.0F = No effect
 * 1.0F = Cognitive abilities hindered, still playable
 * 2.0F = Incapable of anything except attacking
 */
public class InstinctEffectHinderedMind extends InstinctEffect {
    @CapabilityInject(IMorphing.class)
    private static final Capability<IMorphing> MORPHING_CAPABILITY = null;
    
    // Amplitude at which placing blocks becomes disabled
    public static float BLOCK_PLACE_THRESHOLD = 1.5F;
    // Amplitude at which digging with tools becomes disabled
    public static float TOOL_THRESHOLD = 1.75F;
    // Amplitude at which most block/item interaction becomes disabled
    public static float INTERACT_THRESHOLD = 2.0F;
    
    @Override
    public void onActivate(EntityPlayer player, float amplifier) {}

    @Override
    public void onDeactivate(EntityPlayer player, float amplifier) {}

    @Override
    public void tick(EntityPlayer player, float amplifier) {}

    @Override
    public boolean canInteract(EntityPlayer player, float amplifier, BlockPos pos, Block block) {
        return amplifier < INTERACT_THRESHOLD;
    }

    @Override
    public boolean canInteract(EntityPlayer player, float amplifier, ItemStack itemStack) {
        if (itemStack == InventoryUtil.ITEM_STACK_EMPTY) {
            return true;
        }
        
        boolean allowed = true;
        Item item = itemStack.getItem();
        
        if (amplifier >= TOOL_THRESHOLD) {
            // Digging and stuff
            allowed &= !(item instanceof ItemTool);
        }

        if (amplifier >= INTERACT_THRESHOLD) {
            // Right clicking things
            allowed &= (item instanceof ItemFood) ||
                    (item instanceof ItemPotion) ||
                    (item instanceof ItemSword) ||
                    (item instanceof ItemBow && isSkeleton(player)) ||
                    (ModState.isTanLoaded && InventoryUtil.isTANDrink(item));
        }
        
        // No threshold met
        return allowed;
    }
    
    @Override
    public boolean canPlaceBlock(EntityPlayer player, float amplifier, BlockPos blockPos, Block block) {
        return amplifier < BLOCK_PLACE_THRESHOLD;
    }
    
    private boolean isSkeleton(EntityPlayer player) {
        IMorphing morphing = player.getCapability(MORPHING_CAPABILITY, null);
        if (morphing == null) {
            return false;
        }
        AbstractMorph morph = morphing.getCurrentMorph();
        if (!(morph instanceof EntityMorph)) {
            return false;
        }
        return ((EntityMorph)morph).getEntity(player.world) instanceof EntitySkeleton;
    }
}
