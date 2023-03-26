package targoss.hardcorealchemy.item;

import java.util.Collection;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class ContainerItemHandler extends Container {
    protected final ConditionalItemHandler handler;
    
    public ContainerItemHandler(ConditionalItemHandler handler, int[] containerSlotCoords, Collection<Slot> playerSlots) {
        this.handler = handler;

        int n = handler.getSlots();
        MCInventoryWrapper wrapper = new MCInventoryWrapper(handler);
        for (int i = 0; i < n; ++i) {
            addSlotToContainer(handler.createSlot(wrapper, i, containerSlotCoords[2 * i], containerSlotCoords[(2 * i) + 1]));
        }
        for (Slot playerSlot : playerSlots) {
            addSlotToContainer(playerSlot);
        }
    }
    
    public ItemStackHandler getItemHandler() {
        return handler;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
    
    // Shift-click item transfer not implemented
    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return null;
    }

}
