package targoss.hardcorealchemy.creatures.listener;

import java.util.UUID;

import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.MorphAbilityChangeReason;
import targoss.hardcorealchemy.creatures.util.MorphState;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;

public class ListenerWorldHumanity extends HardcoreAlchemyListener {
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    
    public static void onPlayerSparkCreated(EntityPlayer player, AbstractMorph morphTarget) {
        ICapabilityHumanity humanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (humanity == null) {
            return;
        }
        if (humanity.getHasForgottenMorphAbility()) {
            return;
        }
        MorphState.forceForm(HardcoreAlchemyCore.proxy.configs, player, MorphAbilityChangeReason.FORGOT_ABILITY, morphTarget);
        // TODO: Record the player's spark info for their current life in the world capability
    }
    
    public static void onPlayerSparkBroken(UUID owner) {
        // TODO: Implement
    }
    
    public void onPlayerDeath() {
        // TODO: Implement (probably as event listener)
    }
}
