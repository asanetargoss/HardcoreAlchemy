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

import java.util.Collection;
import java.util.Map;

import ca.wescook.nutrition.capabilities.CapInterface;
import ca.wescook.nutrition.capabilities.CapProvider;
import ca.wescook.nutrition.nutrients.Nutrient;
import net.minecraft.block.BlockBed;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.misc.CapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ProviderMisc;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.util.Chat;
import targoss.hardcorealchemy.util.MiscVanilla;
import toughasnails.api.TANPotions;

/**
 * An event listener for miscellaneous changes that
 * don't fit anywhere in particular
 */
public class ListenerSmallTweaks extends ConfiguredListener {
    public ListenerSmallTweaks(Configs configs) {
        super(configs);
    }
    
    @CapabilityInject(ICapabilityMisc.class)
    private static final Capability<ICapabilityMisc> MISC_CAPABILITY = null;
    
    @SubscribeEvent
    public void onAttachMiscCapability(AttachCapabilitiesEvent.Entity event) {
        if (!(event.getObject() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer)(event.getObject());
        // TODO: Use this when determining attachment for other player capabilities too, since the client only needs to know information about themselves for the most part
        if (player.world.isRemote && player != MiscVanilla.getTheMinecraftPlayer()) {
            return;
        }
        event.addCapability(CapabilityMisc.RESOURCE_LOCATION, new ProviderMisc());
    }

    @SubscribeEvent
    public void onHarvestBed(BlockEvent.HarvestDropsEvent event) {
        if (!(event.getState().getBlock() instanceof BlockBed)) {
            return;
        }
        
        EntityPlayer player = event.getHarvester();
        if (player == null) {
            event.setDropChance(0.0F);
            return;
        }
        
        ItemStack heldStack = player.getHeldItemMainhand();
        if (MiscVanilla.isEmptyItemStack(heldStack)) {
            event.setDropChance(0.0F);
            return;
        }
        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, heldStack) <= 0) {
            event.setDropChance(0.0F);
            return;
        }
        Item heldItem = heldStack.getItem();
        if (!heldItem.getToolClasses(heldStack).contains("axe")) {
            event.setDropChance(0.0F);
            return;
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
                decreaseNutrition(player, NUTRITION_DECREASE_PER_THIRST_TICK);
                
                if (!player.world.isRemote) {
                    ICapabilityMisc miscCap =  player.getCapability(MISC_CAPABILITY, null);
                    if (miscCap != null && !miscCap.getHasSeenThirstWarning()) {
                        miscCap.setHasSeenThirstWarning(true);
                        Chat.message(Chat.Type.NOTIFY, (EntityPlayerMP)player, new TextComponentTranslation("hardcorealchemy.diet.dirty_water_warning"));
                    }
                }
                
                break;
            }
        }
        
    }
    
    @Optional.Method(modid=ModState.NUTRITION_ID)
    public static void decreaseNutrition(EntityPlayer player, float amount) {
        CapInterface nutrition = player.getCapability(CapProvider.NUTRITION_CAPABILITY, null);
        if (nutrition == null) {
            return;
        }
        for (Map.Entry<Nutrient, Boolean> enabled : nutrition.getEnabled().entrySet()) {
            if (enabled.getValue()) {
                nutrition.subtract(enabled.getKey(), amount, false);
            }
        }
    }
}
