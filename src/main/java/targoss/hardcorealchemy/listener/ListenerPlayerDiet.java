package targoss.hardcorealchemy.listener;

import static ca.wescook.nutrition.capabilities.CapProvider.NUTRITION_CAPABILITY;
import static targoss.hardcorealchemy.HardcoreAlchemy.LOGGER;

import java.util.Map;

import ca.wescook.nutrition.capabilities.CapInterface;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.nutrients.NutrientList;
import mchorse.metamorph.api.events.MorphEvent;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.util.MorphDiet;

public class ListenerPlayerDiet {
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    // The capability from Metamorph itself
    @CapabilityInject(IMorphing.class)
    public static final Capability<IMorphing> MORPHING_CAPABILITY = null;
    
    // When the player dies, they become human again, so this event reflects that
    @SubscribeEvent
    public void onPlayerJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof EntityPlayerMP && HardcoreAlchemy.isNutritionLoaded) {
            updateMorphDiet((EntityPlayerMP)entity);
        }
    }
    
    // Utility function to update which player nutrients are enabled based on the current morph
    // Also called by ListenerPlayerHumanity
    @Optional.Method(modid=HardcoreAlchemy.NUTRITION_ID)
    public static void updateMorphDiet(EntityPlayerMP player) {
        IMorphing morphing = player.getCapability(MORPHING_CAPABILITY, null);
        CapInterface nutritionCapability = player.getCapability(NUTRITION_CAPABILITY, null);
        if (nutritionCapability == null) {
            return;
        }
        ICapabilityHumanity humanityCapability = player.getCapability(HUMANITY_CAPABILITY, null);
        if (humanityCapability == null) {
            return;
        }
        MorphDiet.Needs needs;
        if (humanityCapability.canMorph()) {
            needs = MorphDiet.PLAYER_NEEDS;
        }
        else {
            needs = MorphDiet.getNeeds(morphing.getCurrentMorph());
        }
        Map<Nutrient, Float> nutrition = nutritionCapability.get();
        Map<Nutrient, Boolean> enabled = nutritionCapability.getEnabled();
        for (Nutrient nutrient : NutrientList.get()) {
            boolean wasEnabled = enabled.get(nutrient);
            boolean isEnabled = needs.containsNutrient(nutrient.name);
            if (!isEnabled) {
                // Make sure nutrient is disabled
                nutritionCapability.setEnabled(nutrient, false, false);
            }
            else if (!wasEnabled) {
                // Re-enable nutrient and set to default
                nutritionCapability.setEnabled(nutrient, true, false);
                nutritionCapability.set(nutrient, 50.0F, false);
            }
        }
        nutritionCapability.resync();
    }
    
    //TODO: Implement dietary restrictions component, including whether to disable hunger/thirst
    // Handlers to stop player from eating foods they don't want to eat
}
