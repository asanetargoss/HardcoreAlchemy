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
