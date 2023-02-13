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
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TESRHumanityPhylactery extends TileEntitySpecialRenderer<TileHumanityPhylactery> {
    
    protected double tickTime = 0;
    // TODO
    private IBakedModel bakedModel;

    private IBakedModel getBakedModel() {
        if (bakedModel == null) {
            bakedModel = Blocks.MODEL_HUMANITY_PHYLACTERY.getBakedModel();
        }
        return bakedModel;
    }

    @Override
    public void renderTileEntityAt(TileHumanityPhylactery te, double x, double y, double z, float partialTicks, int destroyStage) {
        // Translate to block edge
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableRescaleNormal();

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
        GlStateManager.pushMatrix();

        GlStateManager.translate(.5, 0, .5);
        double angle = (Math.PI * 2 * tickTime / 20 / 10) % 360;
        GlStateManager.rotate((float)angle, 0, 1, 0);

        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        World world = te.getWorld();
        // Translate back to world coordinates for Minecraft rendering, but keep block center offset
        // ¯\_(ツ)_/¯
        GlStateManager.translate(-x, -y, -z);

        // TODO: Figure out why this code seems to have no effect, even though this function is being called. It seems Forge is falling back to some other rendering code to render the OBJ statically? This function doesn't get any quads from the OBJ model, and is therefore a no-op
        // TODO: Further research shows that this is rendering with the empty dummy model. Check the logs for errors
        // TODO: Also, check the logs later on to prune the OBJ file for unsupported features
        // TODO: OK I fixed the error in the log, but looking at the code, I think this does nothing, lol
        Tessellator tessellator = Tessellator.getInstance();
        tessellator.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(
                world,
                bakedModel,
                world.getBlockState(te.getPos()),
                te.getPos(),
                Tessellator.getInstance().getBuffer(),
                false);
        tessellator.draw();

        GlStateManager.popMatrix();
    }
}
