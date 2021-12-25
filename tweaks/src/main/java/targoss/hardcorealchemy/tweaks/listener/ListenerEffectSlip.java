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
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.tweaks.event.EventItemUseResult;
import targoss.hardcorealchemy.tweaks.item.Items;
import targoss.hardcorealchemy.util.InventoryUtil;

public class ListenerEffectSlip extends HardcoreAlchemyListener {
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onAttackEntityEvent(AttackEntityEvent event) {
        EventItemUseResult.onAttackEntityEvent(event);
    }
    
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        EventItemUseResult.onItemUseFinish(event);
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

        // Only call this server-side, to prevent desyncs
        if (!player.world.isRemote) {
            player.dropItem(slipStack, false /*unused*/);
            player.setHeldItem(hand, InventoryUtil.ITEM_STACK_EMPTY);
        }
        
        if (effect.getAmplifier() < 1) {
            player.removeActivePotionEffect(Items.POTION_SLIP);
            if (!player.world.isRemote) {
                // Should be true, but just in case...
                EntityPlayerMP playerMP = ((EntityPlayerMP)player);
                playerMP.connection.sendPacket(new SPacketRemoveEntityEffect(playerMP.getEntityId(), effect.getPotion()));
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerInteractEntity(EventItemUseResult event) {
        if (event.result == EnumActionResult.SUCCESS) {
            onUseItemFinished(event.player, event.hand);
        }
    }
}
