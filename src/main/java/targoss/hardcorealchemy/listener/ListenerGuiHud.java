package targoss.hardcorealchemy.listener;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.config.Configs;

public class ListenerGuiHud extends ConfiguredListener {
    public ListenerGuiHud(Configs configs) {
        super(configs);
    }

    private final Minecraft mc = Minecraft.getMinecraft();
    public static final ResourceLocation TILESET = new ResourceLocation("hardcorealchemy:textures/gui/icon_tileset.png");
    
    public static final int HUMANITY_ICONS = 10;
    private static double HUMANITY_3MIN_LEFT = ListenerPlayerHumanity.HUMANITY_3MIN_LEFT;
    public static boolean render_humanity = true;
    public static double humanity = 0.0D;
    public static double max_humanity = 0.0D;
    private Random rand = new Random();
    
    @SubscribeEvent
    public void onRenderOverlayPre(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != ElementType.ARMOR || !render_humanity) {
            return;
        }
        
        if (GuiIngameForge.left_height == 37) {
            // The health render code doesn't make a space for itself when health is zero, which causes our icons to overlap with the TaN icons
            GuiIngameForge.left_height += 12;
        }
        ScaledResolution resolution = event.getResolution();
        int width = resolution.getScaledWidth();
        int height = resolution.getScaledHeight();
        int left = width/2 - 91;
        int top = height - GuiIngameForge.left_height;
        // Bind our tileset and set up graphics state.
        mc.getTextureManager().bindTexture(TILESET);
        GlStateManager.enableBlend();
        for (int i = 1; i <= HUMANITY_ICONS; i++) {
            int y = top;
            if (humanity <= HUMANITY_3MIN_LEFT) {
                y += rand.nextInt(2);
            }
            //TODO: BUG: 10 gets graphically interpreted as 9 (Not sure if the bug still exists. We'll see!)
            if (i*2 <= humanity) {
                // Render full icon
                mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, y, 0, 0, 9, 9);
            }
            else if (i*2 <= humanity+1) {
                // Render partial icon
                mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, y, 9, 0, 9, 9);
            }
            else if (i*2 <= max_humanity) {
                // Render empty icon
                mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, y, 18, 0, 9, 9);
            }
            else {
                // Render dotted icon
                mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, top, 27, 0, 9, 9);
            }
        }
        // Clean up after ourselves. Give the Minecraft GUI their tileset back. Tell them to move.
        GlStateManager.disableBlend();
        mc.getTextureManager().bindTexture(GuiIngameForge.ICONS);
        GuiIngameForge.left_height += 10;
    }
}
