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

package targoss.hardcorealchemy.listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import com.pam.harvestcraft.item.PresserRecipes;

import ca.wescook.nutrition.capabilities.CapInterface;
import ca.wescook.nutrition.capabilities.CapProvider;
import ca.wescook.nutrition.nutrients.Nutrient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.oredict.OreDictionary;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.item.Items;
import targoss.hardcorealchemy.research.Studies;
import targoss.hardcorealchemy.util.MiscVanilla;
import toughasnails.api.TANCapabilities;
import toughasnails.api.TANPotions;
import toughasnails.handler.thirst.ThirstStatHandler;
import toughasnails.thirst.ThirstHandler;

/**
 * An event listener for miscellaneous changes that
 * don't fit anywhere in particular
 */
public class ListenerSmallTweaks extends HardcoreAlchemyListener {
    @CapabilityInject(ICapabilityMisc.class)
    private static final Capability<ICapabilityMisc> MISC_CAPABILITY = null;
    
    @Optional.Method(modid=ModState.ARS_MAGICA_ID)
    public static void addArsMagicaLogToOredict() {
        OreDictionary.registerOre("logWood", am2.defs.BlockDefs.witchwoodLog);
    }
    
    @Optional.Method(modid=ModState.HARVESTCRAFT_ID)
    public static void fixHarvestcraftWoodPaperRecipes() {
        Method registerItemRecipeMethod = null;
        try {
            registerItemRecipeMethod = PresserRecipes.class.getDeclaredMethod("registerItemRecipe", Item.class, Item.class, Item.class);
            registerItemRecipeMethod.setAccessible(true);
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        if (registerItemRecipeMethod != null) {
            Item paper = net.minecraft.init.Items.PAPER;
            try {
                for (ItemStack logStack : OreDictionary.getOres("logWood")) {
                    registerItemRecipeMethod.invoke(null, logStack.getItem(), paper, paper);
                }
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public void postInit(FMLPostInitializationEvent event) {
        if (ModState.isArsMagicaLoaded) {
            addArsMagicaLogToOredict();
        }
        if (ModState.isHarvestCraftLoaded) {
            fixHarvestcraftWoodPaperRecipes();
        }
    }
    
    /**
     * The Obsidian Sheepman from Ad Inferos overrides
     * the Zombie class. This is all fine and dandy until
     * you realize that attacking sheepmen will cause zombies
     * to spawn, which doesn't make sense.
     */
    @SubscribeEvent
    @Optional.Method(modid=ModState.ADINFEROS_ID)
    public void onReinforceObsidianSheepman(ZombieEvent.SummonAidEvent event) {
        if (EntityList.getEntityString(event.getEntity()).equals(ModState.ADINFEROS_ID + ".ObsidianSheepman")) {
            event.setResult(Result.DENY);
        }
    }
    
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
        CapInterface nutrition = player.getCapability(CapProvider.NUTRITION_CAPABILITY, null);
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
    
    @SubscribeEvent
    public void onWaterMobUpdate(LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!entity.isPotionActive(Items.POTION_AIR_BREATHING)) {
            return;
        }
        if (entity.canBreatheUnderwater() ||
                ((entity instanceof EntityLiving) && ((EntityLiving)entity).getNavigator() instanceof PathNavigateSwimmer)
                ) {
            entity.setAir(300);
        }
    }
}
