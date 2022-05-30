/*
 * Copyright 2017-2022 asanetargoss
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

package targoss.hardcorealchemy.tweaks.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import targoss.hardcorealchemy.coremod.CoremodHook;
import targoss.hardcorealchemy.util.InventoryUtil;

public class EventItemUseResult extends Event {
    public final EntityPlayer player;
    public final EnumHand hand;
    public final EnumActionResult result;
    
    public EventItemUseResult(EntityPlayer player, EnumHand hand, EnumActionResult result) {
        this.player = player;
        this.hand = hand;
        this.result = result;
    }
    
    @CoremodHook
    public static void onItemUseResult(EnumActionResult result, CPacketUseEntity packet, EntityPlayer player) {
        EventItemUseResult event = new EventItemUseResult(player, packet.getHand(), result);
        MinecraftForge.EVENT_BUS.post(event);
    }
    
    @CoremodHook
    public static void onItemUseResult(EnumActionResult result, CPacketPlayerTryUseItem packet, EntityPlayer player) {
        ItemStack heldStack = player.getHeldItem(packet.getHand());
        if (InventoryUtil.isEmptyItemStack(heldStack)) {
            return;
        }
        Item heldItem = heldStack.getItem();
        if (InventoryUtil.isHoldRightClickItem(heldItem)) {
            return;
        }
        if (heldStack.stackSize == 0) {
            // Destroy the item now so that the player inventory is left in a reasonable state
            player.setHeldItem(packet.getHand(), InventoryUtil.ITEM_STACK_EMPTY);
            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, heldStack, packet.getHand());
        }
        EventItemUseResult event = new EventItemUseResult(player, packet.getHand(), result);
        MinecraftForge.EVENT_BUS.post(event);
    }
    
    @CoremodHook
    public static void onItemUseResult(EnumActionResult result, CPacketPlayerTryUseItemOnBlock packet, EntityPlayer player) {
        ItemStack heldStack = player.getHeldItem(packet.getHand());
        if (!InventoryUtil.isEmptyItemStack(heldStack) && heldStack.stackSize == 0) {
            // Destroy the item now so that the player inventory is left in a reasonable state
            player.setHeldItem(packet.getHand(), InventoryUtil.ITEM_STACK_EMPTY);
            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, heldStack, packet.getHand());
        }
        EventItemUseResult event = new EventItemUseResult(player, packet.getHand(), result);
        MinecraftForge.EVENT_BUS.post(event);
    }
    
    @CoremodHook
    public static void onPlayerBlockDestroyed(EntityPlayer player) {
        // Digging always occurs with main hand
        EnumHand hand = EnumHand.MAIN_HAND;
        ItemStack heldStack = player.getHeldItem(hand);
        if (InventoryUtil.isEmptyItemStack(heldStack) || heldStack.stackSize == 0) {
            return;
        }
        EventItemUseResult event = new EventItemUseResult(player, hand, EnumActionResult.SUCCESS);
        MinecraftForge.EVENT_BUS.post(event);
    }
    
    public static void onAttackEntityEvent(AttackEntityEvent forgeEvent) {
        EntityPlayer player = forgeEvent.getEntityPlayer();
        if (player == null) {
            return;
        }
        EnumHand hand = EnumHand.MAIN_HAND;
        ItemStack heldStack = player.getHeldItem(hand);
        if (InventoryUtil.isEmptyItemStack(heldStack) || heldStack.stackSize == 0) {
            return;
        }
        // Assume that the player attacked with their main hand and was successful
        EventItemUseResult event = new EventItemUseResult(player, hand, EnumActionResult.SUCCESS);
        MinecraftForge.EVENT_BUS.post(event);
    }

    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish forgeEvent) {
        // We need to juggle item stacks a bit since EventItemUseResult exposes the EnumHand, not the held ItemStack
        EntityLivingBase entity = forgeEvent.getEntityLiving();
        if (entity == null || !(entity instanceof EntityPlayer)) {
            return;
        }
        EnumHand hand = entity.getActiveHand();
        ItemStack oldHeldStack = entity.getHeldItem(hand);
        if (InventoryUtil.isEmptyItemStack(oldHeldStack) || oldHeldStack.stackSize == 0) {
            return;
        }
        {
            ItemStack eventStack = forgeEvent.getResultStack();
            entity.setHeldItem(hand, eventStack); // Cross fingers and hope no entity overrides setHeldItem
            EventItemUseResult event = new EventItemUseResult((EntityPlayer)entity, hand, EnumActionResult.SUCCESS);
            MinecraftForge.EVENT_BUS.post(event);
            ItemStack newHeldStack = entity.getHeldItem(hand); // May have changed
            forgeEvent.setResultStack(newHeldStack);
            entity.setHeldItem(hand, oldHeldStack); // Put back the old heldStack and hope Forge handles things properly
        }
    }
}
