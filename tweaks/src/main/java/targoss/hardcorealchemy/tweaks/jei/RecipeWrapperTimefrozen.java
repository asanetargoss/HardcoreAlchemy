package targoss.hardcorealchemy.tweaks.jei;

import java.util.Arrays;
import java.util.List;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;
import targoss.hardcorealchemy.tweaks.item.RecipeTimefrozen;

public class RecipeWrapperTimefrozen extends BlankRecipeWrapper {
    protected final RecipeTimefrozen recipe;
    
    public RecipeWrapperTimefrozen(RecipeTimefrozen recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> inputs = Arrays.asList(Arrays.copyOf(recipe.recipeTemplate, recipe.recipeTemplate.length));
        int n = inputs.size();
        ItemStack itemToFreeze = new ItemStack(net.minecraft.init.Items.APPLE);
        for (int i = 0; i < n; ++i) {
            ItemStack input = inputs.get(i);
            if (input == RecipeTimefrozen.ITEM_TO_FREEZE_WILDCARD) {
                inputs.set(i, itemToFreeze);
            }
        }
        ingredients.setInputs(ItemStack.class, inputs);
        ItemStack output = RecipeTimefrozen.getTimefrozenItem(itemToFreeze);
        ingredients.setOutput(ItemStack.class, output);
    }

}
