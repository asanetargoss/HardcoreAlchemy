package targoss.hardcorealchemy.listener;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ListenerGui {
    private final Minecraft mc = Minecraft.getMinecraft();
    public static final ResourceLocation TILESET = new ResourceLocation("hardcorealchemy:textures/gui/icon_tileset.png");
    
    public static final int HUMANITY_ICONS = 10;
    public static boolean render_humanity = true;
    public static double humanity = 0.0D;
    public static double max_humanity = 0.0D;
    
    @SubscribeEvent
    public void onRenderOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != ElementType.ARMOR || !render_humanity) {
            return;
        }
        
        ScaledResolution resolution = event.getResolution();
        int width = resolution.getScaledWidth();
        int height = resolution.getScaledHeight();
        int left = width/2 - 91;
        int top = height - GuiIngameForge.left_height;
        // Bind our tileset and set up graphics state.
        mc.getTextureManager().bindTexture(TILESET);
        GlStateManager.enableBlend();
        //TODO: Get this working first, then worry about performance
        //TODO: Jitter effect when humanity is < 0.25 which starts at the rightmost icon and moves left
        //TODO: Represent unfilled icons which exceed max humanity as dotted outlines a la TaN (should not jitter)
        for (int i = 1; i <= HUMANITY_ICONS; i++) {
            //TODO: BUG: 10 gets graphically interpreted as 9
            if (i*2 <= humanity) {
                // Render full icon
                mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, top, 0, 0, 9, 9);
            }
            else if (i*2 <= humanity+1) {
                // Render partial icon
                mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, top, 9, 0, 9, 9);
            }
            else {
                // Render empty icon
                mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, top, 18, 0, 9, 9);
            }
        }
        // Clean up after ourselves. Give the Minecraft GUI their tileset back. Tell them to move.
        GlStateManager.disableBlend();
        mc.getTextureManager().bindTexture(GuiIngameForge.ICONS);
        GuiIngameForge.left_height += 10;
    }
}
