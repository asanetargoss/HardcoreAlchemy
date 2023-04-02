/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

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
