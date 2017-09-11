package targoss.hardcorealchemy.event;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class EventCraftPredict extends Event {
    private boolean canceled = false;
    
    public ItemStack craftResult;
    public final InventoryCrafting craftGrid;
    public final World world;
    
    public EventCraftPredict(ItemStack itemStack, InventoryCrafting inventoryCrafting, World world) {
        this.craftResult = itemStack;
        this.craftGrid = inventoryCrafting;
        this.world = world;
    }
    
    public boolean isCanceled() {
        return canceled;
    }
    
    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
    
    public static ItemStack onCraftPredict(ItemStack craftResult, InventoryCrafting craftGrid, World world) {
        EventCraftPredict event = new EventCraftPredict(craftResult, craftGrid, world);
        return (MinecraftForge.EVENT_BUS.post(event) ? null : event.craftResult);
    }
}
