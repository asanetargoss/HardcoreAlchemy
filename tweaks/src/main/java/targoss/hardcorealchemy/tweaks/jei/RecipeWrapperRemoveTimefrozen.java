package targoss.hardcorealchemy.tweaks.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;
import targoss.hardcorealchemy.tweaks.item.RecipeTimefrozen;

public class RecipeWrapperRemoveTimefrozen extends BlankRecipeWrapper {
    @Override
    public void getIngredients(IIngredients ingredients) {
        ItemStack itemToThaw = new ItemStack(net.minecraft.init.Items.APPLE);
        ItemStack timefrozenItem = RecipeTimefrozen.getTimefrozenItem(itemToThaw);
        List<ItemStack> inputs = new ArrayList<>(1);
        inputs.add(timefrozenItem);
        ingredients.setInputs(ItemStack.class, inputs);
        ingredients.setOutput(ItemStack.class, itemToThaw);
    }
}
