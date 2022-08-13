package targoss.hardcorealchemy.creatures.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RecipeEnchantment {
    public final ItemStack input;
    public final Item subItemsGetter;
    public RecipeEnchantment(Item input, Item outputItemWithSubItems) {
        this.input = new ItemStack(input);
        this.subItemsGetter = outputItemWithSubItems;
    }
}
