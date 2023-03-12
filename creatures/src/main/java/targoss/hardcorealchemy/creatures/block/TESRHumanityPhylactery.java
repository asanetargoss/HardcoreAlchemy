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

package targoss.hardcorealchemy.creatures.block;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TESRHumanityPhylactery extends TileEntitySpecialRenderer<TileHumanityPhylactery> {
    
    protected double tickTime = 0;
    protected static final double ROTATION_FREQUENCY = 0.5;
    private IBakedModel bakedModel;

    private IBakedModel getBakedModel() {
        if (bakedModel == null) {
            bakedModel = Blocks.MODEL_HUMANITY_PHYLACTERY.getBakedModel();
        }
        return bakedModel;
    }
    
    protected void setupCoordFrame(double x, double y, double z) {
        GlStateManager.translate(x, y, z);
        GlStateManager.disableRescaleNormal();
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
    }

    @Override
    public void renderTileEntityAt(TileHumanityPhylactery te, double x, double y, double z, float partialTicks, int destroyStage) {
        // Translate to block edge
        GlStateManager.pushAttrib();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW); // TODO: RAT
        GlStateManager.pushMatrix();

        setupCoordFrame(x, y, z);
        renderFrame(te, x, y, z, tickTime);
        tickTime += partialTicks;

        // Done
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }
    
    protected void renderFrame(TileHumanityPhylactery te, double x, double y, double z, double tickTime) {
        IBakedModel bakedModel = getBakedModel();
        if (bakedModel == null) {
            return;
        }
        
        // TODO: Split the frame into two parts
        // TODO: Use te data to determine if the frame should rotate
        double angle = (ROTATION_FREQUENCY * (Math.PI * 2 * tickTime / 20)) % 360;
        GlStateManager.rotate((float)angle, 0, 1, 0);

        RenderHelper.disableStandardItemLighting();
        bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }
        
        // TODO: Remove debug triangle
        {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glBegin(GL11.GL_TRIANGLES);
            GL11.glVertex3d(-0.5, -0.5, -0.5);
            GL11.glVertex3d( 0.5, -0.5, -0.5);
            GL11.glVertex3d( 0.5,  0.5,  0.5);
            GL11.glVertex3d( 0.5,  0.5,  0.5);
            GL11.glVertex3d( 0.5, -0.5, -0.5);
            GL11.glVertex3d(-0.5, -0.5, -0.5);
            GL11.glEnd();
        }

        World world = te.getWorld();
        // Translate back to world coordinates for Minecraft rendering, but keep block center offset
        // ¯\_(ツ)_/¯
        GlStateManager.translate(-x, -y, -z);

        Tessellator tessellator = Tessellator.getInstance();
        // TODO: Why is this BLOCK and not ITEM like the model being rendered?
        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        // TODO: Figure out what's preventing this model from rendering
        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(
                world,
                bakedModel,
                world.getBlockState(te.getPos()),
                te.getPos(),
                Tessellator.getInstance().getBuffer(),
                false);
        tessellator.draw();

        RenderHelper.enableStandardItemLighting();
    }
}
