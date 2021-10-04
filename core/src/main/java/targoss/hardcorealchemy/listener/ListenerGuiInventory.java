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

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.food.ICapabilityFood;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.util.FoodLists;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.MorphDiet;
import targoss.hardcorealchemy.util.MorphState;

@SideOnly(Side.CLIENT)
public class ListenerGuiInventory extends HardcoreAlchemyListener {
    private final Minecraft mc = Minecraft.getMinecraft();
    
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    
    @CapabilityInject(ICapabilityFood.class)
    public static final Capability<ICapabilityFood> FOOD_CAPABILITY = null;
    
    @SubscribeEvent
    public void onDisplayRestrictionTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        MorphDiet.Restriction itemRestriction = null;

        // We're on the client side. NBT tags are synchronized, but we need to
        // turn it into a capability ourselves.
        ICapabilityFood capabilityFood = CapUtil.getVirtualCapability(itemStack, FOOD_CAPABILITY);
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
    
    // A client-side tooltip when hovering over an uncraftable magic item
    @SubscribeEvent
    public void onTooltipMagicCrafting(ItemTooltipEvent event) {
        GuiScreen gui = mc.currentScreen;
        if (gui == null ||
                !(gui instanceof GuiContainer) ||
                !InventoryUtil.isCraftingSlot(((GuiContainer)gui).theSlot)) {
            return;
        }
        
        if (MorphState.canUseHighMagic(mc.player)) {
            return;
        }
        
        ItemStack craftResult = event.getItemStack();
        if (!ListenerPlayerMagic.isCraftingAllowed(craftResult)) {
            event.getToolTip().add(TextFormatting.DARK_GRAY.toString() + new TextComponentTranslation("hardcorealchemy.magic.disabled.crafttooltip").getUnformattedText());
        }
    }
}
