package targoss.hardcorealchemy.jei;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

public class RecipeHandlerIncantation implements IRecipeHandler<RecipeIncantation> {
    public final RecipeCategoryIncantation category;

    public RecipeHandlerIncantation() {
        this.category = new RecipeCategoryIncantation();
    }
    
    @Override
    public Class<RecipeIncantation> getRecipeClass() {
        return RecipeIncantation.class;
    }

    @Override
    public String getRecipeCategoryUid() {
        return category.getUid();
    }

    @Override
    public String getRecipeCategoryUid(RecipeIncantation recipe) {
        return getRecipeCategoryUid();
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(RecipeIncantation recipe) {
        return new RecipeWrapperIncantation(recipe);
    }

    @Override
    public boolean isRecipeValid(RecipeIncantation recipe) {
        return true;
    }
}
