package targoss.hardcorealchemy.tweaks.jei;

import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;

@JEIPlugin
public class HardcoreAlchemyTweaksJEI extends BlankModPlugin {
    @Override
    public void register(IModRegistry registry) {
        registry.addRecipeHandlers(new RecipeHandlerTimefrozen(), new RecipeHandlerRemoveTimefrozen());
    }
}
