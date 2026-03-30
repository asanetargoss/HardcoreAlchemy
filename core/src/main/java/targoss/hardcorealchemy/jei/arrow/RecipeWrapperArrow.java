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

import static net.minecraft.init.Items.ARROW;
import static net.minecraft.init.Items.BOW;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import targoss.hardcorealchemy.item.RecipeArrow;

public class RecipeWrapperArrow implements IRecipeWrapper {
    protected final RecipeArrow recipe;
    
    public RecipeWrapperArrow(RecipeArrow recipe) {
        this.recipe = recipe;
    }

    @Override
    public void drawAnimations(Minecraft minecraft, int backgroundWidth, int backgroundHeight) {}

    @Override
    public void drawInfo(Minecraft minecraft, int backgroundWidth, int backgroundHeight, int mouseX, int mouseY) {}

    @Override
    public List<FluidStack> getFluidInputs() {
        return null;
    }

    @Override
    public List<FluidStack> getFluidOutputs() {
        return null;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {}

    @Override
    public List<List<ItemStack>> getInputs() {
        List<List<ItemStack>> inputs = new ArrayList<>();
        List<ItemStack> targets = new ArrayList<>();
        targets.add(recipe.input);
        List<ItemStack> projectiles = new ArrayList<>();
        projectiles.add(new ItemStack(ARROW));
        List<ItemStack> weapons = new ArrayList<>();
        weapons.add(new ItemStack(BOW));
        inputs.add(targets);
        inputs.add(projectiles);
        inputs.add(weapons);
        return inputs;
    }

    @Override
    public List<List<ItemStack>> getOutputs() {
        List<List<ItemStack>> outputs = new ArrayList<>();
        List<ItemStack> output = new ArrayList<>();
        output.add(recipe.output);
        outputs.add(output);
        return outputs;
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        return null;
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        return false;
    }

}
