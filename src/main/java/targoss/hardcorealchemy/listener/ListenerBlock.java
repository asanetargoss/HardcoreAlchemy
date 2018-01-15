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

import net.minecraft.block.BlockBed;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.config.Configs;

public class ListenerBlock extends ConfiguredListener {
    public ListenerBlock(Configs configs) {
        super(configs);
    }

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
        if (heldStack == null) {
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
