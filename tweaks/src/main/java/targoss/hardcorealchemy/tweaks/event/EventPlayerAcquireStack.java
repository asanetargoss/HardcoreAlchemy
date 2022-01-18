package targoss.hardcorealchemy.tweaks.event;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class EventPlayerAcquireStack extends Event {
    public final InventoryPlayer inventoryPlayer;
    public final ItemStack itemStack;
    
    public EventPlayerAcquireStack(InventoryPlayer inventoryPlayer, ItemStack itemStack) {
        this.inventoryPlayer = inventoryPlayer;
        this.itemStack = itemStack;
    }
    
    public static boolean onPlayerAcquireStack(InventoryPlayer inventoryPlayer, ItemStack itemStack) {
        EventPlayerAcquireStack event = new EventPlayerAcquireStack(inventoryPlayer, itemStack);
        return !MinecraftForge.EVENT_BUS.post(event);
    }
}
