package targoss.hardcorealchemy.listener;

import static targoss.hardcorealchemy.listener.ListenerPlayerMagic.MAGIC_ITEM_ALLOW_CRAFT;
import static targoss.hardcorealchemy.listener.ListenerPlayerMagic.canUseHighMagic;
import static targoss.hardcorealchemy.listener.ListenerPlayerMagic.isAllowed;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.food.ICapabilityFood;
import targoss.hardcorealchemy.util.FoodLists;
import targoss.hardcorealchemy.util.MorphDiet;

public class ListenerGuiInventory {
    private final Minecraft mc = Minecraft.getMinecraft();
    
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
                !(((GuiContainer)gui).theSlot instanceof SlotCrafting)) {
            return;
        }
        ItemStack craftResult = event.getItemStack();
        if (!canUseHighMagic && !isAllowed(MAGIC_ITEM_ALLOW_CRAFT, craftResult)) {
            event.getToolTip().add(TextFormatting.DARK_GRAY.toString() + new TextComponentTranslation("hardcorealchemy.magic.disabled.crafttooltip").getUnformattedText());
        }
    }
}
