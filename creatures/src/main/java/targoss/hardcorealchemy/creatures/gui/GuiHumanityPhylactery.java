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

package targoss.hardcorealchemy.creatures.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

@SideOnly(Side.CLIENT)
public class GuiHumanityPhylactery extends GuiContainer {
    protected final ITextComponent playerInventoryName;
    
    protected static final ResourceLocation TEXTURE = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "textures/gui/alchemist_core_background.png");
    protected static final ITextComponent LABEL = new TextComponentTranslation(HardcoreAlchemyCore.MOD_ID + ".gui.alchemist_core.label");

    public GuiHumanityPhylactery(Container container, ITextComponent playerInventoryName) {
        super(container);
        this.playerInventoryName = playerInventoryName;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        Minecraft.getMinecraft().fontRendererObj.drawString(LABEL.getFormattedText(), i + 24, j + 6, 0x404040);
        Minecraft.getMinecraft().fontRendererObj.drawString(playerInventoryName.getFormattedText(), i + 8, j + 72, 0x404040);
    }

}
