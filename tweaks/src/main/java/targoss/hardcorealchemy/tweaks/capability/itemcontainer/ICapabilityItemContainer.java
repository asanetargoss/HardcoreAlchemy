package targoss.hardcorealchemy.tweaks.capability.itemcontainer;

import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * NOT an inventory. An item is stored inside, but
 * you aren't supposed to extract it like you would
 * with a chest/backpack/etc.
 */
public interface ICapabilityItemContainer {
    ItemStack getContainedItem();
    void setContainedItem(ItemStack containedItem);
    // TODO: Rename propertyOverrides to something else more sensible
    // TODO: ResourceLocation, not String
    /** For model rendering (initialized on the client side, but stored on the server-side)
     * There are strict limits on its size due to requiring data sent from the client.
     * */
    Map<ResourceLocation, Float> getPropertyOverrides();
    void setPropertyOverrides(Map<ResourceLocation, Float> propertyOverrides);
}
