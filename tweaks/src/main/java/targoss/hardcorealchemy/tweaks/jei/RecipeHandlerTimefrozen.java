package targoss.hardcorealchemy.tweaks.jei;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import targoss.hardcorealchemy.tweaks.item.RecipeTimefrozen;

public class RecipeHandlerTimefrozen implements IRecipeHandler<RecipeTimefrozen> {

    @Override
    public Class<RecipeTimefrozen> getRecipeClass() {
        return RecipeTimefrozen.class;
    }

    @Override
    public String getRecipeCategoryUid() {
        return "minecraft.crafting";
    }

    @Override
    public String getRecipeCategoryUid(RecipeTimefrozen recipe) {
        return "minecraft.crafting";
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(RecipeTimefrozen recipe) {
        return new RecipeWrapperTimefrozen(recipe);
    }

    @Override
    public boolean isRecipeValid(RecipeTimefrozen recipe) {
        return true;
    }

}
