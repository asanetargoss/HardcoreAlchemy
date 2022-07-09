package targoss.hardcorealchemy.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import targoss.hardcorealchemy.incantation.Incantations;

@JEIPlugin
public class HardcoreAlchemyJEI extends BlankModPlugin {
    @Override
    public void register(IModRegistry registry) {
        RecipeHandlerIncantation handlerIncantation = new RecipeHandlerIncantation();
        registry.addRecipeHandlers(handlerIncantation);
        registry.addRecipeCategories(handlerIncantation.category);
        List<Object> recipes = new ArrayList<>();
        recipes.add(Incantations.RECIPE_INCANTATION_CREATE_SLATE);
        registry.addRecipes(recipes);
    }
}
