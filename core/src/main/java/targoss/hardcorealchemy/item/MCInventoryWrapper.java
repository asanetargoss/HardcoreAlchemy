package targoss.hardcorealchemy.item;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.ItemStackHandler;
import targoss.hardcorealchemy.util.InventoryUtil;

public class MCInventoryWrapper implements IInventory {
    protected final ItemStackHandler handler;
    protected final @Nullable Gui gui;
    protected final @Nullable Listener listener;
    
    public static interface Gui {
        public String getName();
        public boolean hasCustomName();
        public ITextComponent getDisplayName();
        public void openInventory(EntityPlayer player);
        public void closeInventory(EntityPlayer player);
    }
    
    public static interface Listener {
        void onInventoryChanged(IInventory inventory);
    }
    
    public MCInventoryWrapper(ItemStackHandler handler) {
        this(handler, null, null);
    }
    
    public MCInventoryWrapper(ItemStackHandler handler, @Nullable Gui gui, @Nullable Listener listener) {
        this.handler = handler;
        this.gui = gui;
        this.listener = listener;
    }

    @Override
    public String getName() {
        if (gui != null) {
            return gui.getName();
        }
        return "InventoryStackHandler name";
    }

    @Override
    public boolean hasCustomName() {
        if (gui != null) {
            return gui.hasCustomName();
        }
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        if (gui != null) {
            return gui.getDisplayName();
        }
        return new TextComponentString("InventoryStackHandler display name");
    }

    @Override
    public int getSizeInventory() {
        return handler.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return handler.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return handler.extractItem(index, count, false);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return handler.extractItem(index, getInventoryStackLimit(), false);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        handler.setStackInSlot(index, stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        if (listener != null) {
            listener.onInventoryChanged(this);
        }
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {
        if (gui != null) {
            gui.openInventory(player);
        }
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if (gui != null) {
            gui.closeInventory(player);
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (InventoryUtil.isEmptyItemStack(stack)) {
            return false;
        }
        ItemStack remainingStack = handler.insertItem(index, stack, true);
        return InventoryUtil.isEmptyItemStack(remainingStack) || remainingStack.stackSize != stack.stackSize || remainingStack.getItem() != stack.getItem();
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        int n = handler.getSlots();
        for (int i = 0; i < n; ++i) {
            handler.setStackInSlot(i, InventoryUtil.ITEM_STACK_EMPTY);
        }
    } 
    
}