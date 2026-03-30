package targoss.hardcorealchemy.item;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RecipeArrow {
    public static Map<Item, RecipeArrow> RECIPES = new HashMap<>();
    
    public final ItemStack input;
    public final ItemStack output;
    public RecipeArrow(Item input, Item output) {
        this.input = new ItemStack(input);
        this.output = new ItemStack(output);
        RECIPES.put(input, this);
    }

}
