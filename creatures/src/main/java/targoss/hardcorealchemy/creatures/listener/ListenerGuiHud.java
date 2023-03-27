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

package targoss.hardcorealchemy.creatures.listener;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.ClientProxy;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ProviderHumanity;
import targoss.hardcorealchemy.creatures.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.creatures.capability.instinct.ProviderInstinct;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.MorphExtension;
import targoss.hardcorealchemy.util.NutritionExtension;
import targoss.hardcorealchemy.util.RandomWithPublicSeed;

@SideOnly(Side.CLIENT)
public class ListenerGuiHud extends HardcoreAlchemyListener {
    private static final Minecraft mc = Minecraft.getMinecraft();
    // Settable seed helps to freeze GUI elements in place when the game is paused
    private RandomWithPublicSeed rand = new RandomWithPublicSeed();
    private long randSeed = rand.getSeed();
    
    public static final int HUMANITY_ICONS = 10;

    // TODO: Render humanity differently when the player is bound to a humanity phylactery
    @SubscribeEvent(priority=EventPriority.HIGHEST,receiveCanceled=true)
    public void onRenderHumanity(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != ElementType.ARMOR) {
            return;
        }
        
        // Freeze GUI elements in place when the game is paused
        // I *think* this event subscriber gets called first due to being defined first in the class.
        // We'll find out soon enough if that's true...
        if (Minecraft.getMinecraft().isGamePaused()) {
            rand.setSeed(randSeed);
        } else {
            randSeed = rand.getSeed();
        }
        
        EntityPlayer player = mc.player;
        
        if (MorphExtension.INSTANCE.isGhost(player)) {
            return;
        }
        
        ICapabilityHumanity humanityCap = player.getCapability(ProviderHumanity.HUMANITY_CAPABILITY, null);
        if (humanityCap == null || !humanityCap.shouldDisplayHumanity()) {
            return;
        }
        
        IAttributeInstance maxHumanityAttribute = player.getEntityAttribute(ICapabilityHumanity.MAX_HUMANITY);
        if (maxHumanityAttribute == null) {
            return;
        }

        double humanity = humanityCap.getHumanity();
        double magicInhibition = humanityCap.getMagicInhibition();
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
        mc.getTextureManager().bindTexture(ClientProxy.TILESET);
        GlStateManager.enableBlend();
        
        boolean drawDottedIcons = MorphExtension.INSTANCE.shouldDrawHumanityDottedIcons();
        
        for (int i = 1; i <= HUMANITY_ICONS; i++) {
            int y = top;
            
            if (humanity <= humanityCap.getHumanityNMinutesLeft(3) && magicInhibition < humanity) {
                y += rand.nextInt(2);
            }
            
            if (i*2 <= magicInhibition) {
                // Full icon
                if (humanity <= (i*2)+1 && humanity > magicInhibition) {
                    // Magic inhibition on left, humanity on right
                    // Per the conservative estimate rule, even though both humanity and magic inhibition are greater than i*2
                    mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, y, 18, 9, 9, 9);
                }
                else {
                    // Magic inhibition only
                    mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, y, 0, 9, 9, 9);
                }
            }
            else if (i*2 <= humanity) {
                // Full icon
                if (humanity <= magicInhibition) {
                    // Magic inhibition only
                    mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, y, 0, 9, 9, 9);
                }
                else if ((i*2)-1 < magicInhibition) {
                    // Magic inhibition on left, humanity on right
                    mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, y, 18, 9, 9, 9);
                }
                else {
                    // Humanity only
                    mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, y, 0, 0, 9, 9);
                }
            }
            else if ((i*2)-1 <= humanity) {
                // Half icon
                if (humanity <= magicInhibition) {
                    // Magic inhibition only
                    mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, y, 0, 9, 9, 9);
                }
                else if ((i-1)*2 < magicInhibition) {
                    // Magic inhibition on left, humanity on right
                    mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, y, 27, 9, 9, 9);
                }
                else {
                    // Humanity only
                    mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, y, 9, 0, 9, 9);
                }
            }
            else if (i*2 <= maxHumanity) {
                if ((i*2)-1 <= magicInhibition) {
                    // Magic inhibition half icon
                    mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, y, 9, 9, 9, 9);
                }
                else {
                    // Empty icon
                    mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, y, 18, 0, 9, 9);
                }
            }
            else {
                if (drawDottedIcons) {
                    // Dotted icon
                    mc.ingameGUI.drawTexturedModalRect(left + (i-1)*8, top, 27, 0, 9, 9);
                }
            }
        }
        
        // Clean up after ourselves. Give the Minecraft GUI their tileset back. Tell them to move.
        GlStateManager.disableBlend();
        mc.getTextureManager().bindTexture(GuiIngameForge.ICONS);
        GuiIngameForge.left_height += 10;
    }
    
    @SubscribeEvent(priority=EventPriority.HIGHEST,receiveCanceled=true)
    public void onRenderInstinct(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != ElementType.ARMOR) {
            return;
        }
        
        EntityPlayer player = mc.player;
        
        if (MorphExtension.INSTANCE.isGhost(player)) {
            return;
        }
        
        ICapabilityInstinct instinctCap = player.getCapability(ProviderInstinct.INSTINCT_CAPABILITY, null);
        if (instinctCap == null || instinctCap.getInstincts().size() == 0) {
            return;
        }
        
        IAttributeInstance maxInstinctAttribute = player.getEntityAttribute(ICapabilityInstinct.MAX_INSTINCT);
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
        mc.getTextureManager().bindTexture(ClientProxy.TILESET);
        GlStateManager.enableBlend();
        
        int texX = 0;
        int texY = 18;
        int sizeX = 4;
        // Whether we should render the normal chains or the chains with the red glow
        texY += instinctCap.getActiveEffects().size() == 0 ? 0 : 9;
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
    
    @SubscribeEvent
    public void onRenderHunger(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != ElementType.FOOD) {
            return;
        }

        EntityPlayer player = mc.player;
        if (player == null) {
            return;
        }

        if (!NutritionExtension.INSTANCE.getNeeds(player).hasHunger) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void onRenderArmorInAfterlife(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == ElementType.ARMOR &&
                mc.player != null && MorphExtension.INSTANCE.isGhost(mc.player)) {
            event.setCanceled(true);
        }
    }
}
