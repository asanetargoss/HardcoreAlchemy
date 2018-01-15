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

package targoss.hardcorealchemy.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class Chat {
    
    public static Style LIGHT_GREY_ITALIC;
    public static Style RED_ITALIC;
    public static Style BLUE_ITALIC;
    
    static {
        LIGHT_GREY_ITALIC = new Style();
        LIGHT_GREY_ITALIC.setItalic(true);
        LIGHT_GREY_ITALIC.setColor(TextFormatting.GRAY);
        RED_ITALIC = new Style();
        RED_ITALIC.setItalic(true);
        RED_ITALIC.setColor(TextFormatting.DARK_RED);
        BLUE_ITALIC = new Style();
        BLUE_ITALIC.setItalic(true);
        BLUE_ITALIC.setColor(TextFormatting.DARK_AQUA);
    }
    
    public static void notify(EntityPlayerMP player, ITextComponent message) {
        player.addChatMessage(message.setStyle(LIGHT_GREY_ITALIC));
    }
    
    public static void notifyMagical(EntityPlayerMP player, ITextComponent message) {
        player.addChatMessage(message.setStyle(BLUE_ITALIC));
    }
    
    public static void alarm(EntityPlayerMP player, ITextComponent message) {
        player.addChatMessage(message.setStyle(RED_ITALIC));
    }
}
