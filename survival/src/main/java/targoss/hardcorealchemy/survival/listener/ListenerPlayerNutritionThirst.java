package targoss.hardcorealchemy.survival.listener;

import java.util.Collection;
import java.util.Map;

import ca.wescook.nutrition.capabilities.CapProvider;
import ca.wescook.nutrition.nutrients.Nutrient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.listener.ListenerPlayerResearch;
import targoss.hardcorealchemy.research.Studies;
import targoss.hardcorealchemy.util.MiscVanilla;
import toughasnails.api.TANCapabilities;
import toughasnails.api.TANPotions;
import toughasnails.handler.thirst.ThirstStatHandler;
import toughasnails.thirst.ThirstHandler;

public class ListenerPlayerNutritionThirst extends HardcoreAlchemyListener {
    
    // (10 points lost per nutrient) / ~30 seconds thirst potion length / 20 ticks per second
    private static final float NUTRITION_DECREASE_PER_THIRST_TICK = 10.0f / 30.0f / 20.0f;
    // Do not calculate decay for that nutrient if it is below this value
    private static final float MIN_NUTRIENT_VALUE = 3.0f;
    
    /**
     * Causes Nutrition loss from Tough As Nails thirst effect
     */
    @SubscribeEvent
    @Optional.Method(modid=ModState.TAN_ID)
    public void onTANThirst(PlayerTickEvent event) {
        if (!ModState.isNutritionLoaded) {
            return;
        }
        
        if (event.phase != Phase.START) {
            return;
        }
        
        EntityPlayer player = event.player;
        if (player.world.isRemote && player != MiscVanilla.getTheMinecraftPlayer()) {
            return;
        }
        
        Collection<PotionEffect> effects = player.getActivePotionEffects();
        for (PotionEffect effect : effects) {
            if (effect.getPotion() == TANPotions.thirst) {
                decreaseNutrition(player, NUTRITION_DECREASE_PER_THIRST_TICK, MIN_NUTRIENT_VALUE);
                
                ListenerPlayerResearch.acquireFactAndSendChatMessage(player, Studies.FACT_DIRTY_WATER_WARNING);
                
                break;
            }
        }
        
    }
    
    protected Boolean tanAttackHandlerExists = null;
    protected boolean doesTanAttackHandlerExist() {
        if (tanAttackHandlerExists != null) {
            return tanAttackHandlerExists;
        }
        try {
            ThirstStatHandler handler = new ThirstStatHandler();
            handler.onPlayerAttackEntity(null);
        } catch (NoSuchMethodError e) {
            tanAttackHandlerExists = false;
            return tanAttackHandlerExists;
        } catch (NullPointerException e) {}
        tanAttackHandlerExists = true;
        return tanAttackHandlerExists;
    }

    /**
     * Simpler implementation of TaN's ThirstStatHandler.onPlayerAttackEntity which does not
     * break mob death sounds. However, it only fires if ThirstStatHandler.onPlayerAttackEntity
     * has been removed from the code, for example via killitwithfire.
     * */
    @SubscribeEvent
    @Optional.Method(modid=ModState.TAN_ID)
    public void onTanThirstAttack(AttackEntityEvent event) {
        if (doesTanAttackHandlerExist()) {
            return;
        }
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) {
            return;
        }
        Entity target = event.getTarget();
        if (!target.canBeAttackedWithItem() || target.hitByEntity(player)) {
            return;
        }
        ThirstHandler thirst = (ThirstHandler)player.getCapability(TANCapabilities.THIRST, null);
        thirst.addExhaustion(0.3F);
    }
    
    @Optional.Method(modid=ModState.NUTRITION_ID)
    public static void decreaseNutrition(EntityPlayer player, float amount, float minValue) {
        ca.wescook.nutrition.capabilities.CapInterface nutrition = player.getCapability(CapProvider.NUTRITION_CAPABILITY, null);
        if (nutrition == null) {
            return;
        }
        boolean shouldSync = false;
        for (Map.Entry<Nutrient, Boolean> enabled : nutrition.getEnabled().entrySet()) {
            if (enabled.getValue()) {
                float currentNutrition = nutrition.get(enabled.getKey());
                float newNutrition = Math.max(minValue, currentNutrition - amount);
                if (newNutrition != currentNutrition) {
                    nutrition.set(enabled.getKey(), newNutrition, false);
                }
                if (Math.round(newNutrition * 4) != Math.round(currentNutrition * 4)) {
                    shouldSync = true;
                }
            }
        }
        if (shouldSync) {
            nutrition.resync();
        }
    }
}
