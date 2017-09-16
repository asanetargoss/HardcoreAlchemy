package targoss.hardcorealchemy.listener;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.food.ICapabilityFood;
import targoss.hardcorealchemy.util.FoodLists;
import targoss.hardcorealchemy.util.MorphDiet;

public class ListenerGuiInventory {
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
}
