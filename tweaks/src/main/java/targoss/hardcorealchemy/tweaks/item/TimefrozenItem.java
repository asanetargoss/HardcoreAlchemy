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

package targoss.hardcorealchemy.tweaks.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.capability.VirtualCapabilityManager;
import targoss.hardcorealchemy.tweaks.capability.itemcontainer.ICapabilityItemContainer;
import targoss.hardcorealchemy.tweaks.capability.itemcontainer.ProviderItemContainer;
import targoss.hardcorealchemy.util.InventoryUtil;

public class TimefrozenItem extends Item {
    @Override
    @SideOnly(Side.CLIENT)
    public boolean hasCustomProperties() {
        return true;
    }
    
    protected Style TIMEFROZEN_STYLE = new Style().setColor(TextFormatting.AQUA);
    
    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        ItemStack containedItem = InventoryUtil.ITEM_STACK_EMPTY;
        ICapabilityItemContainer container = VirtualCapabilityManager.INSTANCE.getVirtualCapability(stack, ProviderItemContainer.CAPABILITY_ITEM_CONTAINER, false);
        if (container != null) {
            containedItem = container.getContainedItem();
        }
        if (InventoryUtil.isEmptyItemStack(containedItem)) {
            containedItem = new ItemStack(net.minecraft.init.Items.APPLE);
        }
        String translationString = getUnlocalizedName() + ".name";
        String containedItemDisplayName = containedItem.getDisplayName();
        ITextComponent text = new TextComponentTranslation(translationString, containedItemDisplayName);
        text.setStyle(TIMEFROZEN_STYLE);
        return text.getFormattedText();
    }
}
