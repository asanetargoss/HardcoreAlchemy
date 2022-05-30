/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.listener;

import net.minecraft.block.BlockBed;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.InventoryUtil;

public class ListenerBedBreakHarvest extends HardcoreAlchemyListener {
    @SubscribeEvent
    public void onHarvestBed(BlockEvent.HarvestDropsEvent event) {
        if (!(event.getState().getBlock() instanceof BlockBed)) {
            return;
        }
        
        EntityPlayer player = event.getHarvester();
        if (player == null) {
            event.setDropChance(0.0F);
            return;
        }
        
        ItemStack heldStack = player.getHeldItemMainhand();
        if (InventoryUtil.isEmptyItemStack(heldStack)) {
            event.setDropChance(0.0F);
            return;
        }
        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, heldStack) <= 0) {
            event.setDropChance(0.0F);
            return;
        }
        Item heldItem = heldStack.getItem();
        if (!heldItem.getToolClasses(heldStack).contains("axe")) {
            event.setDropChance(0.0F);
            return;
        }
    }
}
