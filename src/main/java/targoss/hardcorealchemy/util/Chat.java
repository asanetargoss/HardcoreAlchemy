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
