/*
 * Copyright 2017-2023 asanetargoss
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
