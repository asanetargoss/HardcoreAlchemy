/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Magic.
 *
 * Hardcore Alchemy Magic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Magic is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Magic. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.magic.listener;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.InventoryExtension;

public class ListenerGuiInventory extends HardcoreAlchemyListener {
    private final Minecraft mc = Minecraft.getMinecraft();
    
    // A client-side tooltip when hovering over an uncraftable magic item
    @SubscribeEvent
    public void onTooltipMagicCrafting(ItemTooltipEvent event) {
        GuiScreen gui = mc.currentScreen;
        if (gui == null ||
                !(gui instanceof GuiContainer) ||
                !InventoryExtension.INSTANCE.isCraftingSlot(((GuiContainer)gui).theSlot)) {
            return;
        }
        
        ItemStack craftResult = event.getItemStack();
        if (!ListenerPlayerMagic.isCraftingAllowed(mc.player, craftResult)) {
            event.getToolTip().add(TextFormatting.DARK_GRAY.toString() + new TextComponentTranslation("hardcorealchemy.magic.disabled.crafttooltip").getUnformattedText());
        }
    }
}
