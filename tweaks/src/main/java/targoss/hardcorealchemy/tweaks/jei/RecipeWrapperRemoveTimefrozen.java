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

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;
import targoss.hardcorealchemy.tweaks.item.RecipeTimefrozen;

public class RecipeWrapperRemoveTimefrozen extends BlankRecipeWrapper {
    @Override
    public void getIngredients(IIngredients ingredients) {
        ItemStack itemToThaw = new ItemStack(net.minecraft.init.Items.APPLE);
        ItemStack timefrozenItem = RecipeTimefrozen.getTimefrozenItem(itemToThaw);
        List<ItemStack> inputs = new ArrayList<>(1);
        inputs.add(timefrozenItem);
        ingredients.setInputs(ItemStack.class, inputs);
        ingredients.setOutput(ItemStack.class, itemToThaw);
    }
}
