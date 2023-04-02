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

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class RecipeWrapperIncantation implements IRecipeWrapper {
    public final RecipeIncantation recipe;

    public RecipeWrapperIncantation(RecipeIncantation recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients paramIIngredients) {}

    @Override
    public List<ItemStack> getInputs() {
        List<ItemStack> inputs = new ArrayList<>();
        inputs.add(recipe.input);
        inputs.add(recipe.interact);
        return inputs;
    }

    @Override
    public List<ItemStack> getOutputs() {
        List<ItemStack> outputs = new ArrayList<>();
        outputs.add(recipe.output);
        return outputs;
    }

    @Override
    public List<FluidStack> getFluidInputs() {
        return null;
    }

    @Override
    public List<FluidStack> getFluidOutputs() {
        return null;
    }

    @Override
    public void drawInfo(Minecraft paramMinecraft, int backgroundWidth, int backgroundHeight, int mouseX, int mouseY) {}

    @Override
    public void drawAnimations(Minecraft paramMinecraft, int backgroundWidth, int backgroundHeight) {}

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return null;
    }

    @Override
    public boolean handleClick(Minecraft paramMinecraft, int mouseX, int mouseY, int mouseButton) {
        return false;
    }

}
