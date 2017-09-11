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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
import net.minecraftforge.fml.relauncher.Side;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.food.CapabilityFood;
import targoss.hardcorealchemy.capability.food.ICapabilityFood;
import targoss.hardcorealchemy.capability.food.ProviderFood;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.event.EventCraftPredict;
import targoss.hardcorealchemy.util.FoodLists;
import targoss.hardcorealchemy.util.MorphDiet;

public class ListenerPlayerDiet {
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
        if (entity instanceof EntityPlayerMP && HardcoreAlchemy.isNutritionLoaded) {
            updateMorphDiet((EntityPlayerMP)entity);
        }
    }

    // Utility function to update which player nutrients are enabled based on
    // the current morph
    // Also called by ListenerPlayerHumanity
    @Optional.Method(modid = HardcoreAlchemy.NUTRITION_ID)
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
            } else if (!wasEnabled) {
                // Re-enable nutrient and set to default
                nutritionCapability.setEnabled(nutrient, true, false);
                nutritionCapability.set(nutrient, 50.0F, false);
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
            targoss.hardcorealchemy.util.Chat.notify((EntityPlayerMP)player, needs.restriction.cantEatReason);
        }
    }
    
    @SubscribeEvent
    public void onDisplayRestrictionTooltip(ItemTooltipEvent event) {
        if (!event.getEntity().getEntityWorld().isRemote) {
            return;
        }
        
        ItemStack itemStack = event.getItemStack();
        MorphDiet.Restriction itemRestriction = null;

        // We're on the client side. NBT tags are synchronized, but we need to
        // turn it into a capability ourselves.
        ICapabilityFood capabilityFood = CapUtil.getVirtualCapability(itemStack, FOOD_CAPABILITY);
        if (capabilityFood != null) {
            itemRestriction = capabilityFood.getRestriction();
        }
        else {
            itemRestriction = FoodLists.getRestriction(itemStack);
        }

        if (itemRestriction != null) {
            List<String> tooltips = event.getToolTip();
            tooltips.add(itemRestriction.prettyString);
        }
    }
}
