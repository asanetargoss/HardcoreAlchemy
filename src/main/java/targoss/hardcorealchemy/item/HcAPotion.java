/*
 * Copyright 2017-2018 asanetargoss
 * 
 * This file is part of Hardcore Alchemy.
 * 
 * Hardcore Alchemy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 * 
 * Hardcore Alchemy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hardcore Alchemy.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.item;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.util.Color;

/**
 * A base for custom potion effects.
 */
public class HcAPotion extends Potion {
    private static final ResourceLocation TILESET = new ResourceLocation(HardcoreAlchemy.MOD_ID, "textures/gui/icon_tileset.png");
    private static final int EFFECT_WIDTH = 18;
    private static final int EFFECT_COUNT = 4;
    private static final int REGION_X = 72;
    private static final int REGION_Y = 0;
    private static final int REGION_SIZE = EFFECT_WIDTH*EFFECT_COUNT;
    
    public static final boolean GOOD_EFFECT = false;
    public static final boolean BAD_EFFECT = true;
    
    protected final int iconId;
    protected final double offsetRight;
    
    public HcAPotion(boolean isBadEffect, Color color, int iconId, boolean halfPixelOffsetRight) {
        super(isBadEffect, color.toPackedRGB());
        this.iconId = iconId;
        this.offsetRight = halfPixelOffsetRight ? 0.5D : 0.0D;
    }
    
    @Override
    public boolean isInstant()
    {
        return false;
    }
    
    @Override
    public void affectEntity(@Nullable Entity source, @Nullable Entity indirectSource,
            EntityLivingBase entityLivingBaseIn, int amplifier, double health) { }
    
    @Override
    public boolean isReady(int duration, int amplifier) {
        return true;
    }
    
    /**
     * By default, this method does nothing, and is called every tick 
     * (due to having isInstant() == false and isReady() == true).
     */
    @Override
    public void performEffect(EntityLivingBase entity, int ampifier) { }
    
    /**
     * Render potion effect in inventory using icon in TEXTURES
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventoryEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc) {
        mc.getTextureManager().bindTexture(TILESET);
        float UV_SCALE = 1.0F/256.0F;
        int textureX = REGION_X + EFFECT_WIDTH * (iconId % EFFECT_COUNT);
        int textureY = REGION_Y + EFFECT_WIDTH * (iconId / EFFECT_COUNT);
        double zLevel = 200.0D;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX);
        vertexbuffer.pos((double)(6 + x) + offsetRight,                (double)(7 + y + EFFECT_WIDTH), zLevel).tex((double)((float)(textureX)                * UV_SCALE), (double)((float)(textureY + EFFECT_WIDTH) * UV_SCALE)).endVertex();
        vertexbuffer.pos((double)(6 + x + EFFECT_WIDTH) + offsetRight, (double)(7 + y + EFFECT_WIDTH), zLevel).tex((double)((float)(textureX + EFFECT_WIDTH) * UV_SCALE), (double)((float)(textureY + EFFECT_WIDTH) * UV_SCALE)).endVertex();
        vertexbuffer.pos((double)(6 + x + EFFECT_WIDTH) + offsetRight, (double)(7 + y),                zLevel).tex((double)((float)(textureX + EFFECT_WIDTH) * UV_SCALE), (double)((float)(textureY)                * UV_SCALE)).endVertex();
        vertexbuffer.pos((double)(6 + x) + offsetRight,                (double)(7 + y),                zLevel).tex((double)((float)(textureX)                * UV_SCALE), (double)((float)(textureY)                * UV_SCALE)).endVertex();
        tessellator.draw();
    }
    
    /**
     * Render potion effect on screen using icon in TEXTURES
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void renderHUDEffect(int x, int y, PotionEffect effect, net.minecraft.client.Minecraft mc, float alpha) {
        mc.getTextureManager().bindTexture(TILESET);
        int textureX = REGION_X + EFFECT_WIDTH * (iconId % EFFECT_COUNT);
        int textureY = REGION_Y + EFFECT_WIDTH * (iconId / EFFECT_COUNT);
        mc.ingameGUI.drawTexturedModalRect((float)(x + 3) + (float)offsetRight, (float)(y + 3) + (float)offsetRight, textureX, textureY, EFFECT_WIDTH, EFFECT_WIDTH);
    }
}
