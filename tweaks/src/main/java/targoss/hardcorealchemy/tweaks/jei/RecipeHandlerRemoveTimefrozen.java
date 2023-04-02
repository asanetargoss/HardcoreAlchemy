/*
 * Copyright 2017-2023 asanetargoss
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
