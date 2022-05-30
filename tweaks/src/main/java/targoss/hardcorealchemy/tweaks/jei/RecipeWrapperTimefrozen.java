/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

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
