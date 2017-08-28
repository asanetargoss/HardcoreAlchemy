package targoss.hardcorealchemy.event;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.util.Chat;

/**
 * Function hooks that I didn't want to implement as fully-fledged
 * Forge events.
 */
public class Hooks {
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    
    public static boolean canUseProjectEKeybinds(EntityPlayerMP player) {
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity == null || capabilityHumanity.canUseHighMagic()) {
            return true;
        }
        if (!capabilityHumanity.getNotifiedMagicFail()) {
            capabilityHumanity.setNotifiedMagicFail(true);
            Chat.notify((EntityPlayerMP)player, "Your inhuman form prevents you from manipulating alchemical constructs.");
        }
        return false;
    }
}
