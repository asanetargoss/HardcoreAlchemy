/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.util;

import java.util.Calendar;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.coremod.CoremodHook;

/**
 * Miscellaneous utility class for vanilla Minecraft
 */
public class MiscVanilla {
    @SideOnly(Side.CLIENT)
    @CoremodHook
    public static EntityPlayer getTheMinecraftPlayer() {
        return Minecraft.getMinecraft().player;
    }
    
    @SideOnly(Side.CLIENT)
    public static boolean isTheMinecraftPlayer(EntityPlayer player) {
        return Minecraft.getMinecraft().player == player;
    }
    
    /**
     * Gets the World instance on the current side.
     */
    public static World getWorld() {
        FMLCommonHandler fmlCommonHandler = FMLCommonHandler.instance();
        World world = null;
        if (fmlCommonHandler.getSide() == Side.SERVER ||
                fmlCommonHandler.getEffectiveSide() == Side.SERVER) {
            world = getWorldServer();
        }
        else {
            world = getWorldClient();
        }
        return world;
    }
    
    public static World getWorldServer() {
        if (HardcoreAlchemy.SERVER_REFERENCE == null) {
            return null;
        }
        MinecraftServer server = HardcoreAlchemy.SERVER_REFERENCE.get();
        if (server == null) {
            return null;
        }
        return server.worldServerForDimension(DimensionType.OVERWORLD.getId());
    }
    
    @SideOnly(Side.CLIENT)
    public static World getWorldClient() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) {
            return null;
        }
        EntityPlayer player = mc.player;
        if (player == null) {
            return null;
        }
        return player.world;
    }

    @SideOnly(Side.CLIENT)
    public static String getCurrentLocale() {
        Minecraft minecraft = Minecraft.getMinecraft();
        LanguageManager languageManager = minecraft.getLanguageManager();
        Language currentLanguage = languageManager.getCurrentLanguage();
        String languageCode = currentLanguage.getLanguageCode();
        return languageCode;
    }
    
    public static enum MoonPhase {
        FULL_MOON,
        WANING_GIBBOUS,
        LAST_QUARTER,
        WANING_CRESCENT,
        NEW_MOON,
        WAXING_CRESCENT,
        FIRST_QUARTER,
        WAXING_GIBBOUS;
    }
    
    private static class FuzzState {
        boolean enabled = false;
        Random random = new Random();
        Calendar testCalendar = Calendar.getInstance();
    }
    
    private static FuzzState clientFuzz = new FuzzState();
    private static FuzzState serverFuzz = new FuzzState();
    
    public static void enableTimeFuzz(boolean isRemote) {
        (isRemote ? clientFuzz : serverFuzz).enabled = true;
    }
    
    public static void disableTimeFuzz(boolean isRemote) {
        (isRemote ? clientFuzz : serverFuzz).enabled = false;
    }
    
    public static boolean isFuzzingTime(boolean isRemote) {
        return (isRemote ? clientFuzz : serverFuzz).enabled;
    }

    @CoremodHook
    public static long coremodHookServerTimeMillis(long currentTimeMillis) {
        if (serverFuzz.enabled) {
            // Choose a random Minecraft time
            return serverFuzz.random.nextLong();
        }
        return currentTimeMillis;
    }

    // TODO: @CoremodHook
    public static long coremodHookWorldTimeMillis(long currentTimeMillis, World world) {
        FuzzState state = world.isRemote ? clientFuzz : serverFuzz;
        if (state.enabled) {
            long newTimeMillis;
            do {
                // Choose a random real-world time (excluding April Fools)
                newTimeMillis = state.random.nextLong();
                state.testCalendar.setTimeInMillis(newTimeMillis);
            } while (state.testCalendar.get(Calendar.MONTH) == 4 && state.testCalendar.get(Calendar.DAY_OF_MONTH) == 1);
            return newTimeMillis;
        }
        return currentTimeMillis;
    }
    
    @SideOnly(Side.CLIENT)
    public static boolean getHeldItemTooltips() {
        return Minecraft.getMinecraft().gameSettings.heldItemTooltips;
    }
    
    @SideOnly(Side.CLIENT)
    public static void setHeldItemTooltips(boolean heldItemTooltips) {
        Minecraft.getMinecraft().gameSettings.heldItemTooltips = heldItemTooltips;
    }
    
    public static float getPlayerReachDistance(EntityPlayer player) {
        return 4.5F;
    }
}
