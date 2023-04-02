/*
 * Copyright 2017-2023 asanetargoss
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

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.gui.DrawableBlank;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import targoss.hardcorealchemy.ClientProxy;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.jei.ScaledDrawableResource;

public class RecipeCategoryEnchantment implements IRecipeCategory<RecipeWrapperEnchantment> {
    protected static final String UID = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "enchantment").toString();
    protected final ITextComponent TITLE = new TextComponentTranslation(HardcoreAlchemyCore.MOD_ID + ".recipe.category.enchantment");
    protected final IDrawable BACKGROUND = new DrawableBlank(128, 128);
    protected final IDrawable ICON = new ScaledDrawableResource(256, ClientProxy.TILESET, 180, 0, 16, 16);
    protected final IDrawable CRAFTING_ARROW = new ScaledDrawableResource(256, ClientProxy.TILESET, 144, 16, 22, 16);
    protected final IDrawable INPUT_SLOT = new ScaledDrawableResource(256, ClientProxy.TILESET, 166, 16, 18, 18);
    protected final IDrawable OUTPUT_SLOT = new ScaledDrawableResource(256, ClientProxy.TILESET, 144, 34, 26, 26);
    /** AKA GuiEnchantment.ENCHANTMENT_TABLE_GUI_TEXTURE */
    protected final ResourceLocation ENCHANTMENT_TABLE_GUI_TEXTURE = new ResourceLocation("textures/gui/container/enchanting_table.png");
    protected final int ENCHANTMENT_LEVEL_DRAW_X = 42;
    protected final int ENCHANTMENT_LEVEL_DRAW_Y = 23;
    protected final IDrawable[] ENCHANTMENT_LEVELS = new IDrawable[] {
        new ScaledDrawableResource(256, ENCHANTMENT_TABLE_GUI_TEXTURE, 0, 224, 16, 16), // Level 1
        new ScaledDrawableResource(256, ENCHANTMENT_TABLE_GUI_TEXTURE, 16, 224, 16, 16), // Level 2
        new ScaledDrawableResource(256, ENCHANTMENT_TABLE_GUI_TEXTURE, 32, 224, 16, 16) // Level 3
    };
    
    IGuiIngredient<ItemStack> lapisIngredient = null;
    
    IDrawable getCurrentEnchantmentLevelDrawable() {
        if (lapisIngredient == null) {
            return null;
        }
        ItemStack lapisStack = lapisIngredient.getDisplayedIngredient();
        int enchantmentLevelIndex = lapisStack.stackSize - 1;
        if (enchantmentLevelIndex < 0 || enchantmentLevelIndex >= ENCHANTMENT_LEVELS.length) {
            return null;
        }
        return ENCHANTMENT_LEVELS[enchantmentLevelIndex];
    }

    @Override
    public String getTitle() {
        return TITLE.getUnformattedText();
    }

    @Override
    public String getUid() {
        return UID;
    }

    @Override
    public void drawExtras(Minecraft mc) {
        IDrawable enchantmentLevelDrawable = getCurrentEnchantmentLevelDrawable();
        if (enchantmentLevelDrawable != null) {
            enchantmentLevelDrawable.draw(mc, ENCHANTMENT_LEVEL_DRAW_X, ENCHANTMENT_LEVEL_DRAW_Y);
        }
        
        INPUT_SLOT.draw(mc, 2, 21);
        INPUT_SLOT.draw(mc, 22, 21);
        CRAFTING_ARROW.draw(mc, 68, 21);
        OUTPUT_SLOT.draw(mc, 97, 17);
        
        // Magic enchantment text
        {
            int x = 79;
            int y = 10;
            String s = "LUCK";
            int w = mc.standardGalacticFontRenderer.getStringWidth(s);
            mc.standardGalacticFontRenderer.drawString(s, x - (w / 2), y, 0x444444);
        }
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
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        List<String> tooltipStrings = new ArrayList<>();
        
        IDrawable enchantmentLevelDrawable =  getCurrentEnchantmentLevelDrawable();
        if (enchantmentLevelDrawable != null) {
            if (mouseX >= ENCHANTMENT_LEVEL_DRAW_X &&
                    mouseY >= ENCHANTMENT_LEVEL_DRAW_Y &&
                    mouseX < (ENCHANTMENT_LEVEL_DRAW_X + enchantmentLevelDrawable.getWidth()) &&
                    mouseY < (ENCHANTMENT_LEVEL_DRAW_Y + enchantmentLevelDrawable.getHeight())) {
                int levels = lapisIngredient.getDisplayedIngredient().stackSize;
                String tooltip1Suffix = levels == 1 ? "singular" : "plural";
                Style enchantLevelStyle = new Style().setColor(TextFormatting.GREEN);
                tooltipStrings.add(new TextComponentTranslation(HardcoreAlchemyCore.MOD_ID + ".recipe.category.enchantment.level.tooltip1." + tooltip1Suffix, levels).setStyle(enchantLevelStyle).getFormattedText());
                tooltipStrings.add(new TextComponentTranslation(HardcoreAlchemyCore.MOD_ID + ".recipe.category.enchantment.level.tooltip2").getFormattedText());
                tooltipStrings.add(new TextComponentTranslation(HardcoreAlchemyCore.MOD_ID + ".recipe.category.enchantment.level.tooltip3").getFormattedText());
            }
        }
        
        return tooltipStrings;
    }

    @Override
    public void setRecipe(IRecipeLayout layout, RecipeWrapperEnchantment wrapper) {}

    @Override
    public void setRecipe(IRecipeLayout layout, RecipeWrapperEnchantment wrapper, IIngredients ingredients) {
        layout.getItemStacks().init(0, true, 2, 21); // input (enchantable)
        layout.getItemStacks().init(1, true, 22, 21); // input (lapis)
        layout.getItemStacks().init(2, false, 101, 21); // output (enchanted)
        
        layout.getItemStacks().set(0, (List<ItemStack>)wrapper.getInputs().get(0));
        layout.getItemStacks().set(1, (List<ItemStack>)wrapper.getInputs().get(1));
        IFocus<?> focus = layout.getFocus();
        if (focus.getMode() != IFocus.Mode.OUTPUT) {
            layout.getItemStacks().set(2, (List<ItemStack>)wrapper.getOutputs().get(0));
        }
        else {
            Object o = focus.getValue();
            if (o instanceof ItemStack) {
                layout.getItemStacks().set(2, (ItemStack)o);
            }
        }

        // Figure out the current lapis item displayed by JEI
        IGuiIngredientGroup<ItemStack> ingredientsGroup = layout.getIngredientsGroup(ItemStack.class);
        this.lapisIngredient = ingredientsGroup.getGuiIngredients().get(1);
    }

}
