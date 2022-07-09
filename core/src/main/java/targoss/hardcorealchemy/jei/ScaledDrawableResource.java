package targoss.hardcorealchemy.jei;

import mezz.jei.api.gui.IDrawableStatic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

/**
 * Adapted from DrawableResource in JEI
 */
public class ScaledDrawableResource implements IDrawableStatic {
    protected final ResourceLocation resourceLocation;
    protected final int u;
    protected final int v;
    protected final int width;
    protected final int height;
    protected final int paddingTop;
    protected final int paddingBottom;
    protected final int paddingLeft;
    protected final int paddingRight;
    protected final int resolution;
    
    public ScaledDrawableResource(int resolution, ResourceLocation resourceLocation, int u, int v, int width, int height) { this(resolution, resourceLocation, u, v, width, height, 0, 0, 0, 0); }
    
    
    public ScaledDrawableResource(int resolution, ResourceLocation resourceLocation, int u, int v, int width, int height, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
      this.resolution = resolution;
      this.resourceLocation = resourceLocation;
      
      this.u = u;
      this.v = v;
      this.width = width;
      this.height = height;
      
      this.paddingTop = paddingTop;
      this.paddingBottom = paddingBottom;
      this.paddingLeft = paddingLeft;
      this.paddingRight = paddingRight;
    }
    
    public int getWidth() { return this.width + this.paddingLeft + this.paddingRight; }
    
    public int getHeight() { return this.height + this.paddingTop + this.paddingBottom; }
    
    public void draw(Minecraft minecraft) { draw(minecraft, 0, 0); }

    public void draw(Minecraft minecraft, int xOffset, int yOffset) { draw(minecraft, xOffset, yOffset, 0, 0, 0, 0); }
    
    public void draw(Minecraft minecraft, int xOffset, int yOffset, int maskTop, int maskBottom, int maskLeft, int maskRight) {
      minecraft.getTextureManager().bindTexture(this.resourceLocation);
      
      int x = xOffset + this.paddingLeft + maskLeft;
      int y = yOffset + this.paddingTop + maskTop;
      int u = this.u + maskLeft;
      int v = this.v + maskTop;
      int width = this.width - maskRight - maskLeft;
      int height = this.height - maskBottom - maskTop;
      
      drawTexturedModalRectScaled(this.resolution, x, y, u, v, width, height, 0.0F);
    }

    public static void drawTexturedModalRectScaled(int textureResolution, int x, int y, int u, int v, int width, int height, float zLevel) {
        float uScale = 1f / textureResolution;
        float vScale = 1f / textureResolution;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer wr = tessellator.getBuffer();
        wr.begin(7, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x        , y + height, zLevel).tex( u          * uScale, ((v + height) * vScale)).endVertex();
        wr.pos(x + width, y + height, zLevel).tex((u + width) * uScale, ((v + height) * vScale)).endVertex();
        wr.pos(x + width, y         , zLevel).tex((u + width) * uScale, ( v           * vScale)).endVertex();
        wr.pos(x        , y         , zLevel).tex( u          * uScale, ( v           * vScale)).endVertex();
        tessellator.draw();
    }
}
