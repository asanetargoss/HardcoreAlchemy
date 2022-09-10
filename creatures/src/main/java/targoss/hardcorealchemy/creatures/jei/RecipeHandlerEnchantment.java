/*
 * Copyright 2017-2022 asanetargoss
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

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import targoss.hardcorealchemy.creatures.item.RecipeEnchantment;

public class RecipeHandlerEnchantment implements IRecipeHandler<RecipeEnchantment> {
    public final RecipeCategoryEnchantment category = new RecipeCategoryEnchantment();

    @Override
    public String getRecipeCategoryUid() {
        return category.getUid();
    }

    @Override
    public String getRecipeCategoryUid(RecipeEnchantment arg0) {
        return getRecipeCategoryUid();
    }

    @Override
    public Class<RecipeEnchantment> getRecipeClass() {
        return RecipeEnchantment.class;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(RecipeEnchantment recipe) {
        return new RecipeWrapperEnchantment(recipe);
    }

    @Override
    public boolean isRecipeValid(RecipeEnchantment recipe) {
        return true;
    }

}
