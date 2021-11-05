package targoss.hardcorealchemy.event;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.util.NutritionExtension;

public class MiscHooks {
    public static class ClientSide {
        private static final Minecraft mc = Minecraft.getMinecraft();
        
        @CoremodHook
        public static boolean clientHasThirst() {
            EntityPlayer player = mc.player;
            if (player == null) {
                return true;
            }
            
            return NutritionExtension.INSTANCE.getNeeds(player).hasThirst;
        }
    }
}
