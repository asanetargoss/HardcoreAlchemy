/*
 * Copyright 2017-2026 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.jei.arrow;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import targoss.hardcorealchemy.item.RecipeArrow;

public class RecipeHandlerArrow implements IRecipeHandler<RecipeArrow> {
    public final RecipeCategoryArrow category = new RecipeCategoryArrow();
    
    @Override
    public String getRecipeCategoryUid() {
        return category.getUid();
    }

    @Override
    public String getRecipeCategoryUid(RecipeArrow recipe) {
        return getRecipeCategoryUid();
    }

    @Override
    public Class<RecipeArrow> getRecipeClass() {
        return RecipeArrow.class;
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(RecipeArrow recipe) {
        return new RecipeWrapperArrow(recipe);
    }

    @Override
    public boolean isRecipeValid(RecipeArrow recipe) {
        return true;
    }

}
