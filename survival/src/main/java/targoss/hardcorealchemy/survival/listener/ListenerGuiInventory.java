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

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.capability.VirtualCapabilityManager;
import targoss.hardcorealchemy.capability.food.ICapabilityFood;
import targoss.hardcorealchemy.capability.food.ProviderFood;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.survival.util.FoodLists;
import targoss.hardcorealchemy.util.MorphDiet;

@SideOnly(Side.CLIENT)
public class ListenerGuiInventory extends HardcoreAlchemyListener {
    @SubscribeEvent
    public void onDisplayRestrictionTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        MorphDiet.Restriction itemRestriction = null;

        // We're on the client side. NBT tags are synchronized, but we need to
        // turn it into a capability ourselves.
        ICapabilityFood capabilityFood = VirtualCapabilityManager.INSTANCE.getVirtualCapability(itemStack, ProviderFood.FOOD_CAPABILITY, false);
        if (capabilityFood != null) {
            itemRestriction = capabilityFood.getRestriction();
        }
        else {
            itemRestriction = FoodLists.getRestriction(itemStack);
        }

        if (itemRestriction != null) {
            List<String> tooltips = event.getToolTip();
            tooltips.add(itemRestriction.getFoodTooltip().getFormattedText());
        }
    }
}
