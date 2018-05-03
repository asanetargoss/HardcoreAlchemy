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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    
    public static boolean isEmptyItemStack(ItemStack itemStack) {
        return itemStack == null;
    }
}
