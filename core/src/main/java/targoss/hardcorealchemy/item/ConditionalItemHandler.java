package targoss.hardcorealchemy.item;

import net.minecraft.inventory.IInventory;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

// TODO: It may be possible to just create an instance of SlotItemHandler and then get rid of the slot subclass, so long as the conditional item handling is moved into insertItem (replace calls to createSlot with new SlotItemHandler and see what happens)
public abstract class ConditionalItemHandler extends ItemStackHandler {
    public ConditionalItemHandler(int slotCount) {
        super(slotCount);
    }

    public abstract SlotItemHandler createSlot(IInventory inventory, int index, int xPosition, int yPosition);
}
