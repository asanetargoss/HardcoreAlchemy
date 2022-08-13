package targoss.hardcorealchemy.creatures.jei;

import static net.minecraft.init.Blocks.ENCHANTING_TABLE;
import static net.minecraft.item.Item.getItemFromBlock;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import net.minecraft.item.ItemStack;
import targoss.hardcorealchemy.creatures.item.Items;

@JEIPlugin
public class HardcoreAlchemyCreaturesJEI extends BlankModPlugin {
    @Override
    public void register(IModRegistry registry) {
        RecipeHandlerEnchantment handlerEnchantment = new RecipeHandlerEnchantment();
        registry.addRecipeHandlers(handlerEnchantment);
        registry.addRecipeCategories(handlerEnchantment.category);

        List<Object> recipes = new ArrayList<>();
        recipes.add(Items.RECIPE_ENCHANTMENT_CREATE_SEAL_OF_FORM);
        registry.addRecipes(recipes);
        
        registry.addRecipeCategoryCraftingItem(new ItemStack(getItemFromBlock(ENCHANTING_TABLE)), handlerEnchantment.getRecipeCategoryUid());
    }
}
