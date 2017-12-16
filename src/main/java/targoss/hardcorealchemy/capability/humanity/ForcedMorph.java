package targoss.hardcorealchemy.capability.humanity;

import javax.annotation.Nonnull;

import mchorse.metamorph.api.MorphAPI;
import mchorse.metamorph.api.MorphManager;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.listener.ListenerPlayerDiet;
import targoss.hardcorealchemy.listener.ListenerPlayerHumanity;
import targoss.hardcorealchemy.listener.ListenerPlayerMagic;

public class ForcedMorph {
    public static AbstractMorph createMorph(String morphName) {
        return createMorph(morphName, new NBTTagCompound());
    }
    
    public static AbstractMorph createMorph(String morphName, @Nonnull NBTTagCompound morphProperties) {
        morphProperties.setString("Name", morphName);
        return MorphManager.INSTANCE.morphFromNBT(morphProperties);
    }
    
    public static boolean forceForm(EntityPlayerMP player, LostMorphReason reason,
            String morphName) {
        return forceForm(player, reason, createMorph(morphName));
    }

    public static boolean forceForm(EntityPlayerMP player, LostMorphReason reason,
            String morphName, NBTTagCompound morphProperties) {
        return forceForm(player, reason, createMorph(morphName, morphProperties));
    }

    /**
     * Forces the player into the given AbstractMorph (null permitted)
     * with the given reason, and updates the player's needs
     * Returns true if successful
     */
    public static boolean forceForm(EntityPlayerMP player, LostMorphReason reason,
            AbstractMorph morph) {
        IMorphing morphing = player.getCapability(ListenerPlayerHumanity.MORPHING_CAPABILITY, null);
        if (morphing == null) {
            return false;
        }
        ICapabilityHumanity capabilityHumanity = player.getCapability(ListenerPlayerHumanity.HUMANITY_CAPABILITY, null);
        if (capabilityHumanity == null) {
            return false;
        }
        
        boolean success = true;
        
        AbstractMorph currentMorph = morphing.getCurrentMorph();
        if ((currentMorph == null && morph != null) ||
            (currentMorph != null && morph == null) ||
                (currentMorph != null && morph != null &&
                 !currentMorph.equals(morph))) {
            success = MorphAPI.morph(player, morph, true);
        }
        
        if (success) {
            capabilityHumanity.loseMorphAbilityFor(reason);
            double humanity = morph == null ? 20.0D : 0.0D;
            capabilityHumanity.setHumanity(humanity);
            if (reason != LostMorphReason.LOST_HUMANITY) {
                // Prevent showing the player a message that their humanity has changed
                capabilityHumanity.setLastHumanity(humanity);
            }
            capabilityHumanity.setHighMagicOverride(morph == null ? false : ListenerPlayerHumanity.HIGH_MAGIC_MORPHS.contains(morph.name));
            
            // These morph-specific trait changes only affect players whose form is permanent
            if (!capabilityHumanity.canUseHighMagic()) {
                if (ModState.isArsMagicaLoaded) {
                    ListenerPlayerMagic.eraseSpellMagic(player);
                }
                if (ModState.isProjectELoaded) {
                    ListenerPlayerMagic.eraseEMC(player);
                }
            }
            if (ModState.isNutritionLoaded) {
                ListenerPlayerDiet.updateMorphDiet(player);
            }
        }
        
        return success;
    }

}