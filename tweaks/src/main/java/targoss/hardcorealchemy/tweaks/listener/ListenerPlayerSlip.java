package targoss.hardcorealchemy.tweaks.listener;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketRemoveEntityEffect;
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
    
    public void afterSlipEffectApplied(EntityPlayer player, PotionEffect effect) {
        if (effect.getAmplifier() < 1) {
            player.removeActivePotionEffect(Items.POTION_SLIP);
            if (!player.world.isRemote) {
                // Should be true, but just in case...
                EntityPlayerMP playerMP = ((EntityPlayerMP)player);
                playerMP.connection.sendPacket(new SPacketRemoveEntityEffect(playerMP.getEntityId(), effect.getPotion()));
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
            player.dropItem(slipStack, false /*unused*/);
            player.setHeldItem(hand, InventoryUtil.ITEM_STACK_EMPTY);
        }
        
        afterSlipEffectApplied(player, effect);
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
        player.dropItem(true);
        player.inventory.currentItem = currentSlot;
        
        afterSlipEffectApplied(player, effect);
    }
    
    @SubscribeEvent
    public void onPlayerInteractEntity(EventItemUseResult event) {
        if (event.result == EnumActionResult.SUCCESS) {
            onUseItemFinished(event.player, event.hand);
        }
    }
    
    // TODO: Under slip effect, drop item when switching items (see EntityPlayer.setItemStackToSlot, (TODO: Some case where the slot in a hotbar slot is set to a new item))
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
        
        ItemStack oldSlotStack = event.player.inventory.getStackInSlot(oldSlot);
        if (!InventoryUtil.isEmptyItemStack(oldSlotStack)) {
            onHotbarItemSelectDrop(event.player, oldSlot);
        } else {
            onHotbarItemSelectDrop(event.player, newSlot);
        }
    }
    
    public static class ClientSide {
        // TODO: Make hotbar items jiggle when player has slip effect (see GuiIngame.renderHotbarItem)
    }
}
