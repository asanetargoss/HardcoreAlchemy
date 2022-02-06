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
