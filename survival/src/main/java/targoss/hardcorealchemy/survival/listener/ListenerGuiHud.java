/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Survival.
 *
 * Hardcore Alchemy Survival is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Survival is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Survival. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.survival.listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;

@SideOnly(Side.CLIENT)
public class ListenerGuiHud extends HardcoreAlchemyListener {
    private static final Minecraft mc = Minecraft.getMinecraft();
    
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
