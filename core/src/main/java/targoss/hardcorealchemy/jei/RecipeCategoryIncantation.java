/*
 * Copyright 2017-2022 asanetargoss
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

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.DrawableBlank;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.ClientProxy;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

public class RecipeCategoryIncantation implements IRecipeCategory<RecipeWrapperIncantation> {
    protected static final String UID = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "incantation").toString();
    protected final ITextComponent TITLE = new TextComponentTranslation(HardcoreAlchemyCore.MOD_ID + ".recipe.category.incantation");
    protected final IDrawable ARROW_OUTPUT = new ScaledDrawableResource(256, ClientProxy.TILESET, 144, 0, 20, 15);
    protected final IDrawable BACKGROUND = new DrawableBlank(128, 128);
    protected final IDrawable ICON = new ScaledDrawableResource(256, ClientProxy.TILESET, 164, 0, 16, 16);
    protected final TextComponentTranslation quotedIncantationText = new TextComponentTranslation(HardcoreAlchemyCore.MOD_ID + ".incantation.quoted", new TextComponentTranslation("hardcorealchemy.error_report_to_mod_dev"));
    
    @Override
    public String getUid() {
        return UID;
    }

    @Override
    public String getTitle() {
        return TITLE.getUnformattedText();
    }

    @Override
    public IDrawable getBackground() {
        return BACKGROUND;
    }

    @Override
    public IDrawable getIcon() {
        return ICON;
    }

    @Override
    public void drawExtras(Minecraft mc) {
        ARROW_OUTPUT.draw(mc, ((BACKGROUND.getWidth() - ARROW_OUTPUT.getWidth()) / 2) + 8, 28);
        String toDraw = quotedIncantationText.getUnformattedText();
        int textWidth = mc.fontRendererObj.getStringWidth(toDraw);
        mc.fontRendererObj.drawString(toDraw, (BACKGROUND.getWidth() - textWidth) / 2, 8, 0x444444, false);
    }

    @Override
    public void drawAnimations(Minecraft mc) {}

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, RecipeWrapperIncantation recipeWrapper) {}

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, RecipeWrapperIncantation recipeWrapper,
            IIngredients ingredients) {
        quotedIncantationText.getFormatArgs()[0] = recipeWrapper.recipe.incantation.getCommand();
        
        recipeLayout.getItemStacks().init(0, true, 26, 27); // input
        recipeLayout.getItemStacks().init(1, true, 54, 44); // interact
        recipeLayout.getItemStacks().init(2, false, 83, 27); // output
        
        recipeLayout.getItemStacks().set(0, (ItemStack)recipeWrapper.getInputs().get(0));
        recipeLayout.getItemStacks().set(1, (ItemStack)recipeWrapper.getInputs().get(1));
        recipeLayout.getItemStacks().set(2, (ItemStack)recipeWrapper.getOutputs().get(0));
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        // Item tooltips will be handled via setRecipe
        return new ArrayList<>();
    }

}
