package targoss.hardcorealchemy.tweaks.jei;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import targoss.hardcorealchemy.tweaks.item.RecipeRemoveTimefrozen;

public class RecipeHandlerRemoveTimefrozen implements IRecipeHandler<RecipeRemoveTimefrozen> {

    @Override
    public Class<RecipeRemoveTimefrozen> getRecipeClass() {
        return RecipeRemoveTimefrozen.class;
    }

    @Override
    public String getRecipeCategoryUid() {
        return "minecraft.crafting";
    }

    @Override
    public String getRecipeCategoryUid(RecipeRemoveTimefrozen recipe) {
        return "minecraft.crafting";
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(RecipeRemoveTimefrozen recipe) {
        return new RecipeWrapperRemoveTimefrozen();
    }

    @Override
    public boolean isRecipeValid(RecipeRemoveTimefrozen recipe) {
        return true;
    }

}
