package targoss.hardcorealchemy.creatures.jei;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import targoss.hardcorealchemy.creatures.item.RecipeEnchantment;

public class RecipeHandlerEnchantment implements IRecipeHandler<RecipeEnchantment> {
    public final RecipeCategoryEnchantment category = new RecipeCategoryEnchantment();

    @Override
    public String getRecipeCategoryUid() {
        return category.getUid();
    }

    @Override
    public String getRecipeCategoryUid(RecipeEnchantment arg0) {
        return getRecipeCategoryUid();
    }

    @Override
    public Class<RecipeEnchantment> getRecipeClass() {
        return RecipeEnchantment.class;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(RecipeEnchantment recipe) {
        return new RecipeWrapperEnchantment(recipe);
    }

    @Override
    public boolean isRecipeValid(RecipeEnchantment recipe) {
        return true;
    }

}
