package targoss.hardcorealchemy.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import targoss.hardcorealchemy.util.InventoryUtil.ItemFunc;

/**
 * An overridable utility class for interacting with inventories.
 */
public class InventoryExtension {
    public static InventoryExtension INSTANCE = new InventoryExtension();
    
    /**
     * Check if the slot is a crafting table output slot.
     * This function also handles "technical" slots used by EventDrawInventoryItem.
     */
    public boolean isCraftingSlot(Slot slot) {
        if (slot instanceof SlotCrafting) {
            return true;
        }
        return false;
    }

    public List<IItemHandler> getLocalInventories(Entity entity) {
        if (entity instanceof EntityPlayer) {
            return getLocalInventories((EntityPlayer)entity);
        }
        return new ArrayList<>();
    }

    @Nonnull
    public List<IItemHandler> getLocalInventories(@Nonnull EntityPlayer player) {
        List<IItemHandler> inventories = new ArrayList<>();
        
        // Player main inventory
        if (player.inventory != null) {
            inventories.add(new InvWrapper(player.inventory));
        }
        return inventories;
    }

    @Nonnull
    public List<IItemHandler> getInventories(@Nonnull ItemStack itemStack) {
        List<IItemHandler> inventories = new ArrayList<>();
        
        // Check if the stack has the item handler capability
        {
            IItemHandler inventory = itemStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (inventory != null) {
                inventories.add(inventory);
            }
        }
        
        return inventories;
    }

    @Nonnull
    public List<IItemHandler> getInventories(@Nonnull TileEntity tileEntity) {
        List<IItemHandler> inventories = new ArrayList<>();
        
        IItemHandler inventory = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (inventory != null) {
            inventories.add(inventory);
        }
        else if (tileEntity instanceof IInventory) {
            inventories.add(new InvWrapper((IInventory)tileEntity));
        }
        
        return inventories;
    }

    public List<IItemHandler> getInventories(Entity entity) {
        if (entity instanceof EntityPlayer) {
            return getInventories((EntityPlayer)entity);
        }
        return new ArrayList<>();
    }

    @Nonnull
    public List<IItemHandler> getInventories(@Nonnull EntityPlayer player) {
        List<IItemHandler> inventories = new ArrayList<>();
        
        // Player main inventory
        if (player.inventory != null) {
            inventories.add(new InvWrapper(player.inventory));
        }
        // Ender chest inventory
        {
            IInventory inventory = player.getInventoryEnderChest();
            if (inventory != null) {
                inventories.add(new InvWrapper(inventory));
            }
        }
        
        return inventories;
    }
    
    public static final int DEFAULT_INVENTORY_RECURSION_DEPTH = 6;

    /**
     * Return true if the inventory changed
     */
    public boolean forEachItemRecursive(IItemHandler inventory, ItemFunc itemFunc, int recursionDepth) {
        if (recursionDepth < 0) {
            return false;
        }

        boolean changed = false;
        int n = inventory.getSlots();
        for (int i = 0; i < n; i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            
            if (InventoryUtil.isEmptyItemStack(itemStack)) {
                continue;
            }
            
            changed |= itemFunc.apply(inventory, i, itemStack);

            for (IItemHandler inventoryStack : getInventories(itemStack)) {
                if (inventory == null) {
                    continue;
                }
                changed |= forEachItemRecursive(inventoryStack, itemFunc, recursionDepth - 1);
            }
            
        }
        
        return changed;
    }

    /**
     * Return true if the inventory changed
     */
    public boolean forEachItemRecursive(IItemHandler inventory, InventoryUtil.ItemFunc itemFunc) {
        return forEachItemRecursive(inventory, itemFunc, DEFAULT_INVENTORY_RECURSION_DEPTH);
    }

    /**
     * Return true if the inventory changed
     */
    public boolean forEachItemRecursive(Collection<IItemHandler> inventories, InventoryUtil.ItemFunc itemFunc) {
        boolean changed = false;
        for (IItemHandler inventoryToRecurse : inventories) {
            changed |= forEachItemRecursive(inventoryToRecurse, itemFunc, DEFAULT_INVENTORY_RECURSION_DEPTH);
        }
        return changed;
    }
}
