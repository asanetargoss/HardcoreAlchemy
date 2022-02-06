package targoss.hardcorealchemy.tweaks.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.Event;

public class EventWorkbenchCraft extends Event {
    public final EntityPlayer player;
    public ItemStack craftResult;
    
    public EventWorkbenchCraft(EntityPlayer player, ItemStack craftResult) {
        this.player = player;
        this.craftResult = craftResult;
    }
}
