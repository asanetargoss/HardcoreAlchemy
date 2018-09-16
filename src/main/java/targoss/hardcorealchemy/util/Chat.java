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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Chat {
    public static enum Type {
        NOTIFY(new Style().setItalic(true).setColor(TextFormatting.GRAY)),
        WARN(new Style().setItalic(true).setColor(TextFormatting.DARK_RED)),
        THAUMIC(new Style().setColor(TextFormatting.DARK_PURPLE));
        
        public final Style style;
        Type(Style style) {
            this.style = style;
        }
    }
    
    public static void message(Type type, EntityPlayerMP player, ITextComponent message) {
        message(type, player, message, 0, "");
    }
    
    public static void message(Type type, EntityPlayerMP player, ITextComponent message, int cooldown) {
        message(type, player, message, cooldown, ITextComponent.Serializer.componentToJson(message));
    }
    
    public static void message(Type type, EntityPlayerMP player, ITextComponent message, int cooldown, String cooldownKey) {
        if (!cacheAndDecideDisplayMessage(player, cooldown, cooldownKey)) {
            return;
        }
        
        player.sendMessage(message.setStyle(type.style));
    }
    
    @SideOnly(Side.CLIENT)
    public static void messageSP(Type type, EntityPlayer player, ITextComponent message) {
        messageSP(type, player, message, 0, "");
    }
    
    @SideOnly(Side.CLIENT)
    public static void messageSP(Type type, EntityPlayer player, ITextComponent message, int cooldown) {
        messageSP(type, player, message, cooldown, ITextComponent.Serializer.componentToJson(message));
    }
    
    @SideOnly(Side.CLIENT)
    public static void messageSP(Type type, EntityPlayer player, ITextComponent message, int cooldown, String cooldownKey) {
        if (Minecraft.getMinecraft().player == player) {
            if (!cacheAndDecideDisplayMessage(player, cooldown, cooldownKey)) {
                return;
            }
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(message.setStyle(type.style));
        }
    }
    
    private static Map<GameProfile, Map<String, Integer>> textHistory = new ConcurrentHashMap<>();
    
    public static int MAX_TEXT_HISTORY_SIZE = 10;
    
    private static boolean cacheAndDecideDisplayMessage(EntityPlayer player, int cooldown, String cooldownKey) {
        if (cooldown <= 0) {
            return true;
        }
        
        GameProfile profile = player.getGameProfile();
        Map<String, Integer> history = textHistory.get(profile);
        if (history == null) {
            history = Collections.synchronizedMap(new LinkedHashMap<>());
            textHistory.put(profile, history);
        }
        
        Integer lastMessageTick = history.get(cooldownKey);
        if (lastMessageTick == null || player.ticksExisted - lastMessageTick >= cooldown) {
            if (lastMessageTick != null) {
                // Ensure the key is at the front of the history (since this is an ordered hash map)
                history.remove(cooldownKey);
            }
            history.put(cooldownKey, player.ticksExisted);
            
            if (history.size() > MAX_TEXT_HISTORY_SIZE) {
                String[] toRemove = new String[history.size() - MAX_TEXT_HISTORY_SIZE];
                
                int i = 0;
                for (String key : history.keySet()) {
                    toRemove[i] = key;
                    ++i;
                    if (i >= toRemove.length) {
                        break;
                    }
                }
                
                for (String key : toRemove) {
                    history.remove(key);
                }
            }
            
            return true;
        }
        
        return false;
        
    }
    
    @Deprecated
    public static void notify(EntityPlayerMP player, ITextComponent message) {
        message(Type.NOTIFY, player, message);
    }
    
    @Deprecated
    public static void alarm(EntityPlayerMP player, ITextComponent message) {
        message(Type.WARN, player, message);
    }
    
    @Deprecated
    public static void notifyThaumic(EntityPlayerMP player, ITextComponent message) {
        message(Type.THAUMIC, player, message);
    }
    
    @Deprecated
    @SideOnly(Side.CLIENT)
    public static void notifySP(EntityPlayer player, ITextComponent message) {
        messageSP(Type.NOTIFY, player, message);
    }
    
    @Deprecated
    @SideOnly(Side.CLIENT)
    public static void alarmSP(EntityPlayer player, ITextComponent message) {
        messageSP(Type.WARN, player, message);
    }
    
    @Deprecated
    @SideOnly(Side.CLIENT)
    public static void notifyThaumicSP(EntityPlayer player, ITextComponent message) {
        messageSP(Type.THAUMIC, player, message);
    }
}
