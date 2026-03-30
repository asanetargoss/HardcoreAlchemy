package targoss.hardcorealchemy.jei.arrow;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import targoss.hardcorealchemy.item.RecipeArrow;

public class RecipeHandlerArrow implements IRecipeHandler<RecipeArrow> {
    public final RecipeCategoryArrow category = new RecipeCategoryArrow();
    
    @Override
    public String getRecipeCategoryUid() {
        return category.getUid();
    }

    @Override
    public String getRecipeCategoryUid(RecipeArrow recipe) {
        return getRecipeCategoryUid();
    }

    @Override
    public Class<RecipeArrow> getRecipeClass() {
        return RecipeArrow.class;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(RecipeArrow recipe) {
        return new RecipeWrapperArrow(recipe);
    }

    @Override
    public boolean isRecipeValid(RecipeArrow recipe) {
        return true;
    }

}
