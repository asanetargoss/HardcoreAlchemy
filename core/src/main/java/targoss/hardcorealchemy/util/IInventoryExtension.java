package targoss.hardcorealchemy.util;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;
import targoss.hardcorealchemy.util.InventoryUtil.ItemFunc;

public interface IInventoryExtension {
    /**
     * Check if the slot is a crafting table output slot.
     * This function also handles "technical" slots used by EventDrawInventoryItem.
     */
    boolean isCraftingSlot(Slot slot);
    List<IItemHandler> getLocalInventories(Entity entity);
    @Nonnull List<IItemHandler> getLocalInventories(@Nonnull EntityPlayer player);
    @Nonnull List<IItemHandler> getInventories(@Nonnull ItemStack itemStack);
    @Nonnull List<IItemHandler> getInventories(@Nonnull TileEntity tileEntity);
    List<IItemHandler> getInventories(Entity entity);
    @Nonnull List<IItemHandler> getInventories(@Nonnull EntityPlayer player);
    /**
     * Return true if the inventory changed
     */
    boolean forEachItemRecursive(IItemHandler inventory, ItemFunc itemFunc, int recursionDepth);
    /**
     * Return true if the inventory changed
     */
    boolean forEachItemRecursive(IItemHandler inventory, InventoryUtil.ItemFunc itemFunc);
    /**
     * Return true if the inventory changed
     */
    boolean forEachItemRecursive(Collection<IItemHandler> inventories, InventoryUtil.ItemFunc itemFunc);
}
