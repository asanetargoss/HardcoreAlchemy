/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Survival.
 *
 * Hardcore Alchemy Survival is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Survival is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Survival. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.survival.listener;

import static ca.wescook.nutrition.capabilities.CapProvider.NUTRITION_CAPABILITY;

import java.util.Map;

import ca.wescook.nutrition.capabilities.CapInterface;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.nutrients.NutrientList;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.VirtualCapabilityManager;
import targoss.hardcorealchemy.capability.food.ICapabilityFood;
import targoss.hardcorealchemy.event.EventCraftPredict;
import targoss.hardcorealchemy.event.EventPlayerMorphStateChange;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.survival.util.FoodLists;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.MorphDiet;
import targoss.hardcorealchemy.util.NutritionExtension;
import toughasnails.api.TANCapabilities;
import toughasnails.thirst.ThirstHandler;

public class ListenerPlayerDiet extends HardcoreAlchemyListener {
    @CapabilityInject(ICapabilityFood.class)
    public static final Capability<ICapabilityFood> FOOD_CAPABILITY = null;

    // When the player dies, they become human again, so this event reflects
    // that
    @SubscribeEvent
    public void onPlayerJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof EntityPlayerMP && ModState.isNutritionLoaded) {
            updateMorphDiet((EntityPlayerMP)entity);
        }
    }
    @Optional.Method(modid = ModState.NUTRITION_ID)
    @SubscribeEvent
    public void onPlayerMorphStateChange(EventPlayerMorphStateChange event) {
        updateMorphDiet(event.player);
    }

    // Utility function to update which player nutrients are enabled based on
    // the current morph
    // Also called by ListenerPlayerHumanity
    @Optional.Method(modid = ModState.NUTRITION_ID)
    public static void updateMorphDiet(EntityPlayer player) {
        CapInterface nutritionCapability = player.getCapability(NUTRITION_CAPABILITY, null);
        if (nutritionCapability == null) {
            return;
        }
        MorphDiet.Needs needs = NutritionExtension.INSTANCE.getNeeds(player);
        Map<Nutrient, Boolean> enabled = nutritionCapability.getEnabled();
        for (Nutrient nutrient : NutrientList.get()) {
            // Manage enabled state
            boolean wasEnabled = enabled.get(nutrient);
            boolean isEnabled = needs.containsNutrient(nutrient.name);
            if (!isEnabled) {
                // Make sure nutrient is disabled
                nutritionCapability.setEnabled(nutrient, false, false);
            } else if (!wasEnabled) {
                // Re-enable nutrient and set to default
                nutritionCapability.setEnabled(nutrient, true, false);
                nutritionCapability.set(nutrient, 50.0F, false);
            }
            if (isEnabled) {
                // Have appropriate nutrition depletion for this morph
                nutritionCapability.setDecay(nutrient, needs.getNutrientDecay(nutrient.name), false);
            }
        }
        
        nutritionCapability.resync();
    }

    @SubscribeEvent
    public void onCheckFoodCrafting(EventCraftPredict event) {
        IInventory craftInventory = event.craftGrid;
        ItemStack outputStack = event.craftResult;
        if (InventoryUtil.isEmptyItemStack(outputStack)) {
            return;
        }
        
        if (FoodLists.getRestriction(outputStack) != null) {
            // The dietary restriction is already defined for this item, and can be evaluated dynamically
            return;
        }
        
        if (!FoodLists.isFoodOrIngredient(outputStack)) {
            return;
        }
        
        // Try to figure out a dietary restriction from the crafting ingredients
        MorphDiet.Restriction restriction = null;
        int slotCount = craftInventory.getSizeInventory();
        for (int i = 0; i < slotCount; i++) {
            ItemStack inputStack = craftInventory.getStackInSlot(i);
            if (InventoryUtil.isEmptyItemStack(inputStack)) {
                continue;
            }
            if (!FoodLists.getIgnoresCrafting(inputStack)) {
                MorphDiet.Restriction inputRestriction = null;
                ICapabilityFood inputFoodCap = VirtualCapabilityManager.INSTANCE.getVirtualCapability(inputStack, FOOD_CAPABILITY, false);
                if (inputFoodCap != null) {
                    inputRestriction = inputFoodCap.getRestriction();
                }
                else {
                    inputRestriction = FoodLists.getRestriction(inputStack);
                }
                // A dietary restriction derived from the ingredients. VEGAN
                // + CARNIVORE = OMNIVORE, et cetra
                // Input restrictions and result restriction may be null
                restriction = MorphDiet.Restriction.merge(restriction, inputRestriction);
            }
        }
        
        if (restriction != null) {
            ICapabilityFood outputFoodCap = VirtualCapabilityManager.INSTANCE.getVirtualCapability(outputStack, FOOD_CAPABILITY, true);
            outputFoodCap.setRestriction(restriction);
            VirtualCapabilityManager.INSTANCE.updateVirtualCapability(outputStack, FOOD_CAPABILITY);
        }
    }

    @SubscribeEvent
    public void beforeConsumeFood(PlayerInteractEvent.RightClickItem event) {
        ItemStack itemStack = event.getItemStack();
        if (!(itemStack.getItem() instanceof ItemFood)) {
            return;
        }

        EntityPlayer player = event.getEntityPlayer();
        MorphDiet.Needs needs = NutritionExtension.INSTANCE.getNeeds(player);
        
        ICapabilityFood capabilityFood = VirtualCapabilityManager.INSTANCE.getVirtualCapability(itemStack, FOOD_CAPABILITY, false);
        MorphDiet.Restriction itemRestriction = null;
        if (capabilityFood != null) {
            itemRestriction = capabilityFood.getRestriction();
        }
        else {
            itemRestriction = FoodLists.getRestriction(itemStack);
        }

        if (!needs.restriction.canEat(itemRestriction)) {
            /*TODO: Add a client-side silence period that gets reset
             * each time right click is called, to prevent chat spam,
             * (can use ticksExisted)
             */
            event.setCanceled(true);
            if (player.world.isRemote) {
                Chat.messageSP(Chat.Type.NOTIFY, player, needs.restriction.getFoodRefusal());
            }
        }
    }
    
    /**
     * If a player is a ghost, or is stuck in a morph with no thirst needs,
     * fill thirst so the player never needs to drink
     */
    @SubscribeEvent
    @Optional.Method(modid = ModState.TAN_ID)
    public void onPlayerLoseThirst(PlayerTickEvent event) {
        if (event.phase != Phase.START || event.player.world.isRemote) {
            return;
        }
        
        EntityPlayer player = event.player;
        
        if (!NutritionExtension.INSTANCE.getNeeds(player).hasThirst) {
            ThirstHandler thirstStats = (ThirstHandler)player.getCapability(TANCapabilities.THIRST, null);
            if (thirstStats != null) {
                thirstStats.addStats(20, 20.0F);
                thirstStats.setExhaustion(0.0F);
            }
        }
    }
    
    /**
     * If a player is a ghost or unfeeding, keep hunger filled to spawn level
     */
    @SubscribeEvent
    public void onPlayerLoseHunger(PlayerTickEvent event) {
        if (event.phase != Phase.START || event.player.world.isRemote) {
            return;
        }
        
        EntityPlayer player = event.player;
        
        if (!NutritionExtension.INSTANCE.getNeeds(player).hasHunger) {
            FoodStats food = player.getFoodStats();
            food.setFoodLevel(20);
            food.setFoodSaturationLevel(5.0F);
        }
    }
}
