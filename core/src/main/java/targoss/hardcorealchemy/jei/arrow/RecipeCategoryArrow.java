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

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.DrawableBlank;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import targoss.hardcorealchemy.ClientProxy;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.jei.ScaledDrawableResource;

public class RecipeCategoryArrow implements IRecipeCategory<RecipeWrapperArrow> {
    protected static final String UID = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "arrow").toString();
    protected final ITextComponent TITLE = new TextComponentTranslation(HardcoreAlchemyCore.MOD_ID + ".recipe.category.arrow");
    protected final IDrawable BACKGROUND = new DrawableBlank(128, 128);
    protected final IDrawable ICON = new ScaledDrawableResource(256, ClientProxy.TILESET, 196, 0, 16, 16);
    protected final IDrawable CRAFTING_ARROW = new ScaledDrawableResource(256, ClientProxy.TILESET, 144, 16, 22, 16);
    protected final IDrawable INPUT_SLOT = new ScaledDrawableResource(256, ClientProxy.TILESET, 166, 16, 18, 18);
    protected final IDrawable OUTPUT_SLOT = new ScaledDrawableResource(256, ClientProxy.TILESET, 144, 34, 26, 26);

    @Override
    public String getTitle() {
        return TITLE.getFormattedText();
    }

    @Override
    public String getUid() {
        return UID;
    }

    @Override
    public void drawExtras(Minecraft mc) {
        INPUT_SLOT.draw(mc, 2, 21);
        // Don't draw input slots for the arrow and bow
        CRAFTING_ARROW.draw(mc, 68, 21);
        OUTPUT_SLOT.draw(mc, 97, 17);
    }

    @Override
    public void drawAnimations(Minecraft mc) {}

    @Override
    public IDrawable getBackground() {
        return BACKGROUND;
    }

    @Override
    public IDrawable getIcon() {
        return ICON;
    }

    @Override
    public List<String> getTooltipStrings(int arg0, int arg1) {
        return new ArrayList<>();
    }

    @Override
    public void setRecipe(IRecipeLayout layout, RecipeWrapperArrow wrapper) {}

    @Override
    public void setRecipe(IRecipeLayout layout, RecipeWrapperArrow wrapper, IIngredients ingredients) {
        layout.getItemStacks().init(0, true, 2, 21); // input (target)
        layout.getItemStacks().init(1, true, 22, 21); // input (arrow)
        layout.getItemStacks().init(2, true, 42, 21); // input (bow)
        layout.getItemStacks().init(3, false, 101, 21); // output (shot target)
        
        layout.getItemStacks().set(0, (List<ItemStack>)wrapper.getInputs().get(0));
        layout.getItemStacks().set(1, (List<ItemStack>)wrapper.getInputs().get(1));
        layout.getItemStacks().set(2, (List<ItemStack>)wrapper.getInputs().get(2));
        IFocus<?> focus = layout.getFocus();
        if (focus.getMode() != IFocus.Mode.OUTPUT) {
            layout.getItemStacks().set(3, (List<ItemStack>)wrapper.getOutputs().get(0));
        }
        else {
            Object o = focus.getValue();
            if (o instanceof ItemStack) {
                layout.getItemStacks().set(3, (ItemStack)o);
            }
        }
    }

}
