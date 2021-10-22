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
