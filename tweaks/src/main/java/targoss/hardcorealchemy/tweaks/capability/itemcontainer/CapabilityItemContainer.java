package targoss.hardcorealchemy.tweaks.capability.itemcontainer;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.util.InventoryUtil;

public class CapabilityItemContainer implements ICapabilityItemContainer {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "item_container");
    
    ItemStack containedItem = InventoryUtil.ITEM_STACK_EMPTY;
    Map<ResourceLocation, Float> propertyOverrides = new HashMap<>();

    @Override
    public ItemStack getContainedItem() {
        return containedItem;
    }

    @Override
    public void setContainedItem(ItemStack containedItem) {
        this.containedItem = containedItem;
    }

    @Override
    public Map<ResourceLocation, Float> getPropertyOverrides() {
        return propertyOverrides;
    }

    @Override
    public void setPropertyOverrides(Map<ResourceLocation, Float> propertyOverrides) {
        this.propertyOverrides = propertyOverrides;
    }

}
