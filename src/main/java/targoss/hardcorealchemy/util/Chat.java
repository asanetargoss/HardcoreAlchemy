package targoss.hardcorealchemy.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class Chat {
    
    public static Style LIGHT_GREY_ITALIC;
    public static Style RED_ITALIC;
    
    static {
        LIGHT_GREY_ITALIC = new Style();
        LIGHT_GREY_ITALIC.setItalic(true);
        LIGHT_GREY_ITALIC.setColor(TextFormatting.GRAY);
        RED_ITALIC = new Style();
        RED_ITALIC.setItalic(true);
        RED_ITALIC.setColor(TextFormatting.DARK_RED);
    }
    
    public static void notify(EntityPlayerMP player, String message) {
        ((EntityPlayerMP)player).addChatMessage(new TextComponentString(message).setStyle(LIGHT_GREY_ITALIC));
    }
    
    public static void alarm(EntityPlayerMP player, String message) {
        ((EntityPlayerMP)player).addChatMessage(new TextComponentString(message).setStyle(RED_ITALIC));
    }
}