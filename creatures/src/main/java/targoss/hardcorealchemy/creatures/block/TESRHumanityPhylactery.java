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
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

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
    
    @Override
    public void renderTileEntityAt(TileHumanityPhylactery te, double playerToBlockX, double playerToBlockY, double playerToBlockZ, float partialTicks, int destroyStage) {
        // Translate to block edge
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();

        renderFrame(te, playerToBlockX, playerToBlockY, playerToBlockZ, tickTime);
        tickTime += partialTicks;

        // Done
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }
    
    protected void renderFrame(TileHumanityPhylactery te, double playerToBlockX, double playerToBlockY, double playerToBlockZ, double tickTime) {
        IBakedModel bakedModel = getBakedModel();
        if (bakedModel == null) {
            return;
        }

        RenderHelper.disableStandardItemLighting();
        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        // Translate from camera to block center
        GlStateManager.translate(playerToBlockX, playerToBlockY, playerToBlockZ);
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        // Rotate the block about its center
        // TODO: Split the frame into two parts
        // TODO: Use te data to determine if the frame should rotate
        double angle = (ROTATION_FREQUENCY * (Math.PI * 2 * tickTime / 20)) % 360;
        GlStateManager.rotate((float)angle, 0, 1, 0);
        // Translate back to world coordinates for Minecraft rendering, but keep block center offset
        // ¯\_(ツ)_/¯
        BlockPos blockPos = te.getPos();
        GlStateManager.translate(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ());

        // TODO: Figure out if there is a way to put this in the baked model
        bindTexture(new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "models/block/humanity_phylactery.png"));
        
        Tessellator tessellator = Tessellator.getInstance();
        // NOTE: Switching the vertex format to ITEM here causes the backside of the model to appear dark
        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        World world = te.getWorld();
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
