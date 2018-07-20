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

package targoss.hardcorealchemy.listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.instincts.ICapabilityInstinct;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.util.MorphState;
import toughasnails.handler.thirst.ThirstOverlayHandler;
import targoss.hardcorealchemy.util.MorphDiet;
import targoss.hardcorealchemy.util.MorphState;

@SideOnly(Side.CLIENT)
public class ListenerGuiHud extends ConfiguredListener {
    public ListenerGuiHud(Configs configs) {
        super(configs);
    }
    
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static final ResourceLocation TILESET = new ResourceLocation(HardcoreAlchemy.MOD_ID, "textures/gui/icon_tileset.png");
    private Random rand = new Random();
    
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    public static final IAttribute MAX_HUMANITY = ICapabilityHumanity.MAX_HUMANITY;
    
    public static final int HUMANITY_ICONS = 10;
    private static double HUMANITY_3MIN_LEFT = ListenerPlayerHumanity.HUMANITY_3MIN_LEFT;

    @SubscribeEvent(priority=EventPriority.HIGHEST,receiveCanceled=true)
    public void onRenderHumanity(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != ElementType.ARMOR) {
            return;
        }
        
        EntityPlayer player = mc.player;
        
        if (ModState.isDissolutionLoaded && MorphState.isIncorporeal(player)) {
            return;
        }
        
        ICapabilityHumanity humanityCap = player.getCapability(HUMANITY_CAPABILITY, null);
        if (humanityCap == null || !humanityCap.shouldDisplayHumanity()) {
            return;
        }
        
        IAttributeInstance maxHumanityAttribute = player.getEntityAttribute(MAX_HUMANITY);
        if (maxHumanityAttribute == null) {
            return;
        }

        double humanity = humanityCap.getHumanity();
        double maxHumanity = maxHumanityAttribute.getAttributeValue();
        
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
                //TODO: Freeze shaking in place when the game is paused
                y += rand.nextInt(2);
            }
            if (i*2 <= humanity) {
                // Render full icon
                mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, y, 0, 0, 9, 9);
            }
            else if (i*2 <= humanity+1) {
                // Render partial icon
                mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, y, 9, 0, 9, 9);
            }
            else if (i*2 <= maxHumanity) {
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
    
    @CapabilityInject(ICapabilityInstinct.class)
    public static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;
    public static final IAttribute MAX_INSTINCT = ICapabilityInstinct.MAX_INSTINCT;
    
    @SubscribeEvent(priority=EventPriority.HIGHEST,receiveCanceled=true)
    public void onRenderInstinct(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != ElementType.ARMOR) {
            return;
        }
        
        EntityPlayer player = mc.player;
        
        if (ModState.isDissolutionLoaded && MorphState.isIncorporeal(player)) {
            return;
        }
        
        ICapabilityInstinct instinctCap = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinctCap == null || instinctCap.getInstinctMap().size() == 0) {
            return;
        }
        
        IAttributeInstance maxInstinctAttribute = player.getEntityAttribute(MAX_INSTINCT);
        if (maxInstinctAttribute == null) {
            return;
        }
        
        float instinct = Math.min(instinctCap.getInstinct(), (float)maxInstinctAttribute.getAttributeValue());
        
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
        
        int texX = 0;
        int texY = 18;
        int sizeX = 4;
        // Whether we should render the normal chains or the chains with the red glow
        texY += instinctCap.getActiveInstinct() == null ? 0 : 9;
        int y = top;
        
        if (instinct <= 2.0f) {
            // Only render up to a single chain link
            texX = 54;
            for (int i = 0; i < instinct; i++) {
                mc.ingameGUI.drawTexturedModalRect(left+i*4, y, texX, texY, sizeX, 9);
                if (sizeX == 5) {
                    texX += 5;
                    sizeX = 4;
                }
                else {
                    texX += 4;
                    sizeX = 5;
                }
            }
        }
        else {
            // Render first chain link with overlay. How the rest is rendered depends on the scenario
            texX = 0;
            int instinctRounded = (int)Math.ceil(instinct);
            int specialEndRender = instinctRounded;
            boolean sidewaysChain = instinctRounded % 4 == 0;
            if (instinctRounded % 2 == 0) {
                // Need to render the ending half of a chain
                specialEndRender--;
                if (!sidewaysChain) {
                    // Need to render one even less, to prevent rendering the next sideways chain
                    specialEndRender--;
                }
            }
            int i = 0;
            
            for (i = 0; i < specialEndRender; i++) {
                mc.ingameGUI.drawTexturedModalRect(left+i*4, y, texX, texY, sizeX, 9);
                if (sizeX == 5) {
                    texX += 5;
                    sizeX = 4;
                }
                else {
                    texX += 4;
                    sizeX = 5;
                }
                if (texX >= 27) {
                    texX = 9;
                }
            }
            if (specialEndRender < instinctRounded) {
                // Break out of loop for special render of final link with no overlap on the right side
                if (sidewaysChain) {
                    // Scenario 1: Length in icons is "even": Final chain link is sideways.
                    int prevSizeX = sizeX == 5 ? 4 : 5;
                    texX = 27+prevSizeX;
                    mc.ingameGUI.drawTexturedModalRect(left+i*4, y, texX, texY, sizeX, 9);
                }
                else {
                    // Scenario 2: Length in icons is "odd": Final chain link is flat relative to the screen.
                    texX = 36;
                    mc.ingameGUI.drawTexturedModalRect(left+i*4-1, y, texX, texY, 18, 9);
                }
            }
        }
        
        // Clean up after ourselves. Give the Minecraft GUI their tileset back. Tell them to move.
        GlStateManager.disableBlend();
        mc.getTextureManager().bindTexture(GuiIngameForge.ICONS);
        GuiIngameForge.left_height += 10;
    }
    
    @CoremodHook
    @Optional.Method(modid = ModState.TAN_ID)
    public static boolean clientHasThirst() {
        EntityPlayer player = mc.player;
        if (player == null) {
            return true;
        }
        
        if (ModState.isDissolutionLoaded && MorphState.isIncorporeal(player)) {
            return false;
        }
        
        ICapabilityHumanity humanityCap = player.getCapability(HUMANITY_CAPABILITY, null);
        if (humanityCap == null || humanityCap.getHumanity() > 0) {
            return true;
        }
        
        IMorphing morphing = Morphing.get(player);
        if (morphing == null) {
            return true;
        }
        
        return MorphDiet.getNeeds(morphing.getCurrentMorph()).hasThirst;
    }
    
    @SubscribeEvent
    public void onRenderArmorInAfterlife(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == ElementType.ARMOR &&
                ModState.isDissolutionLoaded &&
                mc.player != null && MorphState.isIncorporeal(mc.player)) {
            event.setCanceled(true);
        }
    }
    
    private static Object thirstOverlayHandler = null;
    
    @Optional.Method(modid=ModState.TAN_ID)
    private static toughasnails.handler.thirst.ThirstOverlayHandler getThirstOverlayHandler() {
        if (thirstOverlayHandler == null) {
            thirstOverlayHandler = new toughasnails.handler.thirst.ThirstOverlayHandler();
        }
        return (toughasnails.handler.thirst.ThirstOverlayHandler)thirstOverlayHandler;
    }
    
    @SubscribeEvent
    @Optional.Method(modid=ModState.TAN_ID)
    public void onTanThirstHandlerTick(ClientTickEvent event) {
        getThirstOverlayHandler().onClientTick(event);
    }

    private static Method drawThirstMethod = null;
    
    @Optional.Method(modid=ModState.TAN_ID)
    public static void drawThirst(int width, int height, int thirstLevel, float thirstHydrationLevel) {
        try {
            if (drawThirstMethod == null) {
                drawThirstMethod = toughasnails.handler.thirst.ThirstOverlayHandler.class
                        .getDeclaredMethod("drawThirst", int.class, int.class, int.class, float.class);
                drawThirstMethod.setAccessible(true);
            }
            
            drawThirstMethod.invoke(getThirstOverlayHandler(),
                    width, height, thirstLevel, thirstHydrationLevel);
            
        } catch (NoSuchMethodException | SecurityException |
                IllegalAccessException | IllegalArgumentException |
                InvocationTargetException e) {
            e.printStackTrace();
            return;
        }
    }
    
    /**
     * Method adapted from ThirstOverlayHandler.onPreRenderOverlay on the TAN-4.x.x branch
     */
    @Optional.Method(modid=ModState.TAN_ID)
    @CoremodHook
    public static void onRenderThirst(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == ElementType.AIR &&
                toughasnails.api.config.SyncedConfig.getBooleanValue(
                        toughasnails.api.config.GameplayOption.ENABLE_THIRST) &&
                mc.playerController.gameIsSurvivalOrAdventure())
        {
            EntityPlayerSP player = mc.player;
            toughasnails.thirst.ThirstHandler thirstStats =
                    (toughasnails.thirst.ThirstHandler)(player
                    .getCapability(toughasnails.api.TANCapabilities.THIRST, null));
            
            ScaledResolution resolution = event.getResolution();
            mc.getTextureManager().bindTexture(toughasnails.handler.thirst.ThirstOverlayHandler.OVERLAY);
            drawThirst(resolution.getScaledWidth(), resolution.getScaledHeight(),
                    thirstStats.getThirst(), thirstStats.getHydration());
            GuiIngameForge.right_height += 10;
            mc.getTextureManager().bindTexture(GuiIngameForge.ICONS);
        }
    }
}
