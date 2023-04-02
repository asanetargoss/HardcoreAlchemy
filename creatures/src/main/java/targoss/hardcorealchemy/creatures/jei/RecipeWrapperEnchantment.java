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

import static net.minecraft.init.Items.DYE;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import targoss.hardcorealchemy.creatures.item.ItemSealOfForm;
import targoss.hardcorealchemy.creatures.item.RecipeEnchantment;

public class RecipeWrapperEnchantment implements IRecipeWrapper {
    protected final RecipeEnchantment recipe;
    
    public RecipeWrapperEnchantment(RecipeEnchantment recipe) {
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
        List<ItemStack> enchantables = new ArrayList<>();
        enchantables.add(recipe.input);
        List<ItemStack> lapisLevels = new ArrayList<>();
        lapisLevels.add(new ItemStack(DYE, 1, EnumDyeColor.BLUE.getDyeDamage()));
        lapisLevels.add(new ItemStack(DYE, 2, EnumDyeColor.BLUE.getDyeDamage()));
        lapisLevels.add(new ItemStack(DYE, 3, EnumDyeColor.BLUE.getDyeDamage()));
        inputs.add(enchantables);
        inputs.add(lapisLevels);
        return inputs;
    }
    
    protected List<ItemStack> SEAL_OF_FORM_TYPES;

    protected List<ItemStack> getSealOfFormTypes() {
        if (SEAL_OF_FORM_TYPES == null)
        {
            SEAL_OF_FORM_TYPES = new ArrayList<>();
            recipe.subItemsGetter.getSubItems(recipe.subItemsGetter, null, SEAL_OF_FORM_TYPES);
            // The inactive variant of the seal of true form is not craftable via enchanting, so don't include it here.
            // TODO: Why didn't this work?
            SEAL_OF_FORM_TYPES.remove(ItemSealOfForm.getSealHuman(false));
        }
        return SEAL_OF_FORM_TYPES;
    }
    
    @Override
    public List<List<ItemStack>> getOutputs() {
        List<List<ItemStack>> outputs = new ArrayList<>();
        outputs.add(getSealOfFormTypes());
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
