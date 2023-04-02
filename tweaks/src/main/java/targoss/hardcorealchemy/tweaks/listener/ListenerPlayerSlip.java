/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.listener;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ProviderMisc;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.tweaks.event.EventHeldItemChange;
import targoss.hardcorealchemy.tweaks.event.EventItemUseResult;
import targoss.hardcorealchemy.tweaks.event.EventPlayerAcquireStack;
import targoss.hardcorealchemy.tweaks.event.EventPlayerInventorySlotSet;
import targoss.hardcorealchemy.tweaks.item.Items;
import targoss.hardcorealchemy.util.InventoryUtil;

public class ListenerPlayerSlip extends HardcoreAlchemyListener {
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onAttackEntityEvent(AttackEntityEvent event) {
        EventItemUseResult.onAttackEntityEvent(event);
    }
    
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        EventItemUseResult.onItemUseFinish(event);
    }
    
    protected static final String[] UNSLIPPABLE_ITEMS = new String[] {
        "minecraft:shield",
        ModState.ARS_MAGICA_ID + ":bound_shield",
        "voidcraft:items/voidcrystalshield"
    };
    
    public void checkSlipEffectCanceled(EntityPlayer player, PotionEffect effect, int persistAmplifier) {
        boolean shouldCancelEffect;
        if (persistAmplifier > 0) {
            shouldCancelEffect = effect.getAmplifier() < persistAmplifier;
        } else if (effect.getAmplifier() == 0) {
            // Small chance for amplifier to go away
            shouldCancelEffect = player.getRNG().nextFloat() < 0.18F;
        } else {
            shouldCancelEffect = false;
        }
        if (shouldCancelEffect) {
            if (!player.world.isRemote) {
                player.removePotionEffect(Items.POTION_SLIP);
            }
        }
    }
    
    public void onUseItemFinished(EntityPlayer player, EnumHand hand) {
        // When a player with the Slip effect uses an item, they drop the item after use (if it is still there)
        // If an item is actually dropped, and the effect is Slip Level 1 (amplifier = 0), remove the effect
        if (player.isCreative()) {
            return;
        }
        PotionEffect effect = player.getActivePotionEffect(Items.POTION_SLIP);
        if (effect == null) {
            return;
        }
        ItemStack slipStack = player.getHeldItem(hand);
        if (InventoryUtil.isEmptyItemStack(slipStack)) {
            return;
        }
        
        // Don't make the player let go of their shield
        String slipId = slipStack.getItem().getRegistryName().toString();
        for (String unslippableItem : UNSLIPPABLE_ITEMS) {
            if (unslippableItem.equals(slipId)) {
                return;
            }
        }

        // Only call this server-side, to prevent desyncs
        if (!player.world.isRemote) {
            Object itemDropped = player.dropItem(slipStack, false /*unused*/);
            player.setHeldItem(hand, InventoryUtil.ITEM_STACK_EMPTY);
            
            if (itemDropped != null) {
                checkSlipEffectCanceled(player, effect, 1);
            }
        }
    }
    
    public void onHotbarItemSelectDrop(EntityPlayer player, int slotToDrop) {
        if (player.isCreative()) {
            return;
        }
        PotionEffect effect = player.getActivePotionEffect(Items.POTION_SLIP);
        if (effect == null) {
            return;
        }
        
        // Drop the item in the player's hotbar in the given hotbar slot.
        // Switch temporarily to the hotbar slot if needed.
        int currentSlot = player.inventory.currentItem;
        player.inventory.currentItem = slotToDrop;
        Object itemDropped = player.dropItem(true);
        player.inventory.currentItem = currentSlot;
        
        if (itemDropped != null) {
            checkSlipEffectCanceled(player, effect, 0);
        }
    }
    
    @SubscribeEvent
    public void onPlayerInteractEntity(EventItemUseResult event) {
        if (event.result == EnumActionResult.SUCCESS) {
            onUseItemFinished(event.player, event.hand);
        }
    }
    
    @SubscribeEvent
    public void onPlayerChangeHeldItemPre(EventHeldItemChange.Pre event) {
        ICapabilityMisc misc = event.player.getCapability(ProviderMisc.MISC_CAPABILITY, null);
        if (misc == null) {
            return;
        }
        // Cache player's old slot
        misc.setLastItem(event.player.inventory.currentItem);
    }
    
    @SubscribeEvent
    public void onPlayerChangeHeldItemPost(EventHeldItemChange.Post event) {
        ICapabilityMisc misc = event.player.getCapability(ProviderMisc.MISC_CAPABILITY, null);
        if (misc == null) {
            return;
        }
        // Should not be -1. If it is, that is a bug
        int oldSlot = misc.getLastItem();
        int newSlot = event.packet.getSlotId();
        misc.setLastItem(newSlot);
        
        if (newSlot == oldSlot) {
            return;
        }
        
        ItemStack newSlotStack = event.player.inventory.getStackInSlot(newSlot);
        if (!InventoryUtil.isEmptyItemStack(newSlotStack)) {
            onHotbarItemSelectDrop(event.player, newSlot);
        } else {
            onHotbarItemSelectDrop(event.player, oldSlot);
        }
    }

    @SubscribeEvent
    public void onPlayerInventorySlotSet(EventPlayerInventorySlotSet event) {
        if (!InventoryUtil.isHotbarSlotIndex(event.slotIndex)) {
            return;
        }
        if (InventoryUtil.isEmptyItemStack(event.itemStack)) {
            return;
        }
        EntityPlayer player = event.inventoryPlayer.player;
        PotionEffect effect = player.getActivePotionEffect(Items.POTION_SLIP);
        if (effect == null) {
            return;
        }
        if (event.itemStack.stackSize == 0) {
            // The stack size can be 0. Weird things happen if you try to do stuff with it...
            return;
        }
        // Only do this if this is the player's held slot
        if (event.inventoryPlayer.currentItem != event.slotIndex) {
            return;
        }
        if (!player.world.isRemote) {
            // NOTE: I now set event.itemStack to empty *before* the player drops the item in-world.
            // Hopefully this prevents the dupe bug encountered in testing.
            // Further testing with the change in place revealed one occasion where an item
            // was deleted instead.
            // Hopefully the hilarity of temporarily failing to pick up a bunch of items makes up for this.
            // This was probably caused by a race condition, most likely not caused by this code.
            ItemStack trueStack = event.itemStack.copy();
            event.itemStack = InventoryUtil.ITEM_STACK_EMPTY;
            player.dropItem(trueStack, false);
            checkSlipEffectCanceled(player, effect, 0);
        } else {
            // Desync workaround
            event.itemStack = InventoryUtil.ITEM_STACK_EMPTY;
        }
    }
    
    @SubscribeEvent
    public void onPlayerAcquireStack(EventPlayerAcquireStack event) {
        ItemStack itemStack = event.itemStack;
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return;
        }
        if (itemStack.stackSize == 0) {
            return;
        }
        if (itemStack.getItem() == null) {
            return;
        }
        InventoryPlayer inventoryPlayer = event.inventoryPlayer;
        EntityPlayer player = inventoryPlayer.player;
        PotionEffect effect = player.getActivePotionEffect(Items.POTION_SLIP);
        if (effect == null) {
            return;
        }
        // Guess where the stack is about to go by mirroring the InventoryPlayer logic
        // and use that to determine if the item should be kept
        int insertionSlot = InventoryUtil.getInsertionSlot(inventoryPlayer, itemStack);
        // Only do this if this is the player's held slot
        if (event.inventoryPlayer.currentItem != insertionSlot) {
            return;
        }
        event.setCanceled(true);
        checkSlipEffectCanceled(player, effect, 0);
    }
}
