/**
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

import static ca.wescook.nutrition.capabilities.CapProvider.NUTRITION_CAPABILITY;
import static targoss.hardcorealchemy.HardcoreAlchemy.LOGGER;

import java.util.List;
import java.util.Map;

import ca.wescook.nutrition.capabilities.CapInterface;
import ca.wescook.nutrition.nutrients.Nutrient;
import ca.wescook.nutrition.nutrients.NutrientList;
import mchorse.metamorph.api.events.MorphEvent;
import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Chat;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.food.CapabilityFood;
import targoss.hardcorealchemy.capability.food.ICapabilityFood;
import targoss.hardcorealchemy.capability.food.ProviderFood;
import targoss.hardcorealchemy.capability.humanity.ForcedMorph;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.event.EventCraftPredict;
import targoss.hardcorealchemy.util.FoodLists;
import targoss.hardcorealchemy.util.MorphDiet;
import toughasnails.api.TANCapabilities;
import toughasnails.thirst.ThirstHandler;

public class ListenerPlayerDiet extends ConfiguredListener {
    public ListenerPlayerDiet(Configs configs) {
        super(configs);
    }
    
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    @CapabilityInject(ICapabilityFood.class)
    public static final Capability<ICapabilityFood> FOOD_CAPABILITY = null;
    // The capability from Metamorph itself
    @CapabilityInject(IMorphing.class)
    public static final Capability<IMorphing> MORPHING_CAPABILITY = null;

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

    // Utility function to update which player nutrients are enabled based on
    // the current morph
    // Also called by ListenerPlayerHumanity
    @Optional.Method(modid = ModState.NUTRITION_ID)
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
        ItemStack itemStack = event.craftResult;
        if (itemStack == null) {
            return;
        }
        
        if (FoodLists.getRestriction(itemStack) != null) {
            // The dietary restriction is already defined for this item, and can be evaluated dynamically
            return;
        }
        
        // Try to figure out a dietary restriction from the crafting ingredients
        MorphDiet.Restriction restriction = null;
        int slotCount = craftInventory.getSizeInventory();
        for (int i = 0; i < slotCount; i++) {
            ItemStack inputStack = craftInventory.getStackInSlot(i);
            if (inputStack == null) {
                continue;
            }
            if (!FoodLists.getIgnoresCrafting(inputStack)) {
                MorphDiet.Restriction inputRestriction = null;
                ICapabilityFood inputFoodCap = CapUtil.getVirtualCapability(inputStack, FOOD_CAPABILITY);
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
            ICapabilityFood capabilityFood = FOOD_CAPABILITY.getDefaultInstance();
            capabilityFood.setRestriction(restriction);
            CapUtil.setVirtualCapability(itemStack, FOOD_CAPABILITY, capabilityFood);
        }
    }

    @SubscribeEvent
    public void beforeConsumeFood(PlayerInteractEvent.RightClickItem event) {
        if (event.getWorld().isRemote) {
            return;
        }

        ItemStack itemStack = event.getItemStack();
        if (!(itemStack.getItem() instanceof ItemFood)) {
            return;
        }

        EntityPlayer player = event.getEntityPlayer();
        ICapabilityHumanity capabilityHumanity = player.getCapability(HUMANITY_CAPABILITY, null);
        if (capabilityHumanity == null || capabilityHumanity.canMorph()) {
            return;
        }
        
        IMorphing morphing = Morphing.get(player);
        AbstractMorph morph = null;
        if (morphing != null) {
            morph = morphing.getCurrentMorph();
        }
        MorphDiet.Needs needs = MorphDiet.getNeeds(morph);
        
        ICapabilityFood capabilityFood = CapUtil.getVirtualCapability(itemStack, FOOD_CAPABILITY);
        MorphDiet.Restriction itemRestriction = null;
        if (capabilityFood != null) {
            itemRestriction = capabilityFood.getRestriction();
        }
        else {
            itemRestriction = FoodLists.getRestriction(itemStack);
        }

        if (!needs.restriction.canEat(itemRestriction)) {
            event.setCanceled(true);
            targoss.hardcorealchemy.util.Chat.notify((EntityPlayerMP)player, needs.restriction.getFoodRefusal());
        }
    }
    
    /**
     * If a player is a ghost, or is stuck in a morph with no thirst needs,
     * fill thirst so the player never needs to drink
     */
    @SubscribeEvent
    @Optional.Method(modid = ModState.TAN_ID)
    public void onPlayerLoseThirst(PlayerTickEvent event) {
        if (event.phase != Phase.START || event.player.worldObj.isRemote) {
            return;
        }
        
        EntityPlayer player = event.player;
        boolean preventLosingThirst = false;
        
        if (ModState.isDissolutionLoaded && ForcedMorph.isIncorporeal(player)) {
            preventLosingThirst = true;
        }
        
        if (!preventLosingThirst) {
            ICapabilityHumanity humanity = player.getCapability(HUMANITY_CAPABILITY, null);
            if (humanity != null && humanity.canMorph()) {
                IMorphing morphing = Morphing.get(player);
                if (morphing != null &&
                        !MorphDiet.getNeeds(morphing.getCurrentMorph()).hasThirst) {
                    preventLosingThirst = true;
                }
            }
        }
        
        if (preventLosingThirst) {
            ThirstHandler thirstStats = (ThirstHandler)player.getCapability(TANCapabilities.THIRST, null);
            if (thirstStats != null) {
                thirstStats.addStats(20, 20.0F);
                thirstStats.setExhaustion(0.0F);
            }
        }
    }
    
    /**
     * If a player is a ghost, keep hunger filled to spawn level
     */
    @SubscribeEvent
    @Optional.Method(modid = ModState.TAN_ID)
    public void onPlayerLoseHunger(PlayerTickEvent event) {
        if (event.phase != Phase.START || event.player.worldObj.isRemote) {
            return;
        }
        
        EntityPlayer player = event.player;
        
        if (!ModState.isDissolutionLoaded || !ForcedMorph.isIncorporeal(player)) {
            return;
        }
        
        FoodStats food = player.getFoodStats();
        food.setFoodLevel(20);
        food.setFoodSaturationLevel(5.0F);
        food.addExhaustion(-40.0F);
    }
}
