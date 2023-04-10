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

package targoss.hardcorealchemy.creatures.block;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

@SideOnly(Side.CLIENT)
public class TESRHumanityPhylactery extends TileEntitySpecialRenderer<TileHumanityPhylactery> {
    
    // TODO: Don't store per-tile variables here. Store in the tile entity
    protected double tickTime = 0;
    protected Random rand = new Random();
    protected static final float PARTICLE_INTERVAL = 1.5F;
    protected static IBakedModel outerFrame;
    protected static IBakedModel innerFrame;
    protected static int outerFrameDisplayList = -1;
    protected static int innerFrameDisplayList = -1;

    protected static IBakedModel getOuterFrame() {
        if (outerFrame == null) {
            outerFrame = Blocks.MODEL_HUMANITY_PHYLACTERY_OUTER_FRAME.getBakedModel();
        }
        return outerFrame;
    }

    protected static IBakedModel getInnerFrame() {
        if (innerFrame == null) {
            innerFrame = Blocks.MODEL_HUMANITY_PHYLACTERY_INNER_FRAME.getBakedModel();
        }
        return innerFrame;
    }
    
    protected static void renderOuterFrame(TileEntity te, World world) {
        IBakedModel outerFrame = getOuterFrame();
        if (outerFrame == null) {
            return;
        }
        
        if (outerFrameDisplayList == -1) {
            outerFrameDisplayList = GLAllocation.generateDisplayLists(1);
            GlStateManager.glNewList(outerFrameDisplayList, 4864);

            Tessellator vertexbuffer = Tessellator.getInstance();
            // NOTE: Switching the vertex format to ITEM here causes the backside of the model to appear dark
            vertexbuffer.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(
                    world,
                    outerFrame,
                    world.getBlockState(te.getPos()),
                    te.getPos(),
                    Tessellator.getInstance().getBuffer(),
                    false);
            vertexbuffer.draw();

            GlStateManager.glEndList();
        }
        
        GlStateManager.callList(outerFrameDisplayList);
    }
    
    protected static void renderInnerFrame(TileEntity te, World world) {
        IBakedModel innerFrame = getInnerFrame();
        if (innerFrame == null) {
            return;
        }
        
        if (innerFrameDisplayList == -1) {
            innerFrameDisplayList = GLAllocation.generateDisplayLists(1);
            GlStateManager.glNewList(innerFrameDisplayList, 4864);

            Tessellator vertexbuffer = Tessellator.getInstance();
            // NOTE: Switching the vertex format to ITEM here causes the backside of the model to appear dark
            vertexbuffer.getBuffer().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModel(
                    world,
                    innerFrame,
                    world.getBlockState(te.getPos()),
                    te.getPos(),
                    Tessellator.getInstance().getBuffer(),
                    false);
            vertexbuffer.draw();

            GlStateManager.glEndList();
        }
        
        GlStateManager.callList(innerFrameDisplayList);
    }
    
    @Override
    public void renderTileEntityAt(TileHumanityPhylactery te, double playerToBlockX, double playerToBlockY, double playerToBlockZ, float partialTicks, int destroyStage) {
        boolean active = te.isVisiblyActive();
        if (!active)
        {
            te.particleTime = 0;
        }
        renderFrame(te, playerToBlockX, playerToBlockY, playerToBlockZ);
        if (active)
        {
            tickTime += partialTicks;
            te.particleTime += partialTicks;
        }
    }
    
    protected void renderFrame(TileHumanityPhylactery te, double playerToBlockX, double playerToBlockY, double playerToBlockZ) {
        IBakedModel innerFrame = getInnerFrame();
        if (innerFrame == null) {
            return;
        }
        
        RenderHelper.disableStandardItemLighting();
        if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        } else {
            GlStateManager.shadeModel(GL11.GL_FLAT);
        }
        
        // TODO: Use te data to determine if the frame should rotate
        // TODO: Rotational offset depending on the placement of the block
        // TODO: Smoothly enable rotation on activation, and randomize using some seed
        double angleOuter = (TileHumanityPhylactery.ROTATION_FREQUENCY * (Math.PI * 2 * tickTime / 20)) % 360;
        double angleInner = (-1.0 * TileHumanityPhylactery.ROTATION_FREQUENCY * (Math.PI * 2 * tickTime / 20)) % 360;

        World world = te.getWorld();
        BlockPos blockPos = te.getPos();
        
        if (te.particleTime > 0)
        {
            // Spherically random particles
            // TODO: Custom particles
            if ((rand.nextFloat())*te.particleTime > (PARTICLE_INTERVAL / 2))
            {
                te.particleTime -= PARTICLE_INTERVAL;
                float radius = rand.nextFloat();
                radius *= radius;
                radius = 0.2F * (1.0F - radius);
                // Not sure if these are actual Euler angles
                float angleYaw = (float)Math.PI * 2.0F * rand.nextFloat();
                float anglePitch = (float)Math.PI * 2.0F * rand.nextFloat();
                float cosYaw = (float)Math.cos(angleYaw);
                float cosPitch = (float)Math.cos(anglePitch);
                float sinYaw = (float)Math.sin(angleYaw);
                float sinPitch = (float)Math.sin(anglePitch);
                float ax = cosYaw * cosPitch;
                float ay = sinYaw;
                float az = sinPitch;
                world.spawnParticle(EnumParticleTypes.FLAME, 0.5 + blockPos.getX() + (radius * ax), 0.5 + blockPos.getY() + (radius * ay), 0.5 + blockPos.getZ() + (radius * az), 0, 0, 0, 0);
            }
        }
        
        // TODO: Different texture when on?
        bindTexture(new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "models/block/alchemist_core.png"));
        
        // Render outer frame
        GlStateManager.pushMatrix();
        {
            // Translate from camera to block center
            GlStateManager.translate(playerToBlockX, playerToBlockY, playerToBlockZ);
            GlStateManager.translate(0.5F, 0.5F, 0.5F);
            // Rotate about the block center
            GlStateManager.rotate((float)angleOuter, 0, 1, 0);
            // Translate back to world coordinates for Minecraft rendering, but keep block center offset
            // ¯\_(ツ)_/¯
            GlStateManager.translate(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ());
            
            renderOuterFrame(te, world);
        }
        GlStateManager.popMatrix();
        
        // Render inner frame
        GlStateManager.pushMatrix();
        {
            // Translate from camera to block center
            GlStateManager.translate(playerToBlockX, playerToBlockY, playerToBlockZ);
            GlStateManager.translate(0.5F, 0.5F, 0.5F);
            // Rotate about the block center
            GlStateManager.rotate((float)angleInner, 0, 1, 0);
            // Translate back to world coordinates for Minecraft rendering, but keep block center offset
            // ¯\_(ツ)_/¯
            GlStateManager.translate(-blockPos.getX(), -blockPos.getY(), -blockPos.getZ());
    
            renderInnerFrame(te, world);
        }
        GlStateManager.popMatrix();

        RenderHelper.enableStandardItemLighting();
    }
}
