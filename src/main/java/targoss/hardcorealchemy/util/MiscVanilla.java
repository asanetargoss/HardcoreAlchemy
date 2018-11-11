/*
 * Copyright 2018 asanetargoss
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

package targoss.hardcorealchemy.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.HardcoreAlchemy;

/**
 * Miscellaneous utility class for vanilla Minecraft
 */
public class MiscVanilla {
    @SideOnly(Side.CLIENT)
    public static EntityPlayer getTheMinecraftPlayer() {
        return Minecraft.getMinecraft().player;
    }
    
    @SideOnly(Side.CLIENT)
    public static boolean isTheMinecraftPlayer(EntityPlayer player) {
        return Minecraft.getMinecraft().player == player;
    }
    
    public static final ItemStack ITEM_STACK_EMPTY = null;
    
    public static boolean isEmptyItemStack(ItemStack itemStack) {
        return itemStack == null;
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
    public static boolean getHeldItemTooltips() {
        return Minecraft.getMinecraft().gameSettings.heldItemTooltips;
    }
    
    @SideOnly(Side.CLIENT)
    public static void setHeldItemTooltips(boolean heldItemTooltips) {
        Minecraft.getMinecraft().gameSettings.heldItemTooltips = heldItemTooltips;
    }
}
