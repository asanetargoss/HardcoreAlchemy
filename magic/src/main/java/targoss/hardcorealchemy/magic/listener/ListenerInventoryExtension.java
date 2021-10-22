package targoss.hardcorealchemy.magic.listener;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;

import am2.container.slot.SlotMagiciansWorkbenchCrafting;
import gr8pefish.ironbackpacks.capabilities.IronBackpacksCapabilities;
import gr8pefish.ironbackpacks.capabilities.player.PlayerWearingBackpackCapabilities;
import gr8pefish.ironbackpacks.container.backpack.InventoryBackpack;
import gr8pefish.ironbackpacks.items.backpacks.ItemBackpack;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.InventoryExtension;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.InventoryUtil.ItemFunc;
import thaumcraft.common.container.slot.SlotCraftingArcaneWorkbench;

public class ListenerInventoryExtension extends HardcoreAlchemyListener {
    public static class Wrapper extends InventoryExtension {
        public InventoryExtension delegate;
        
        public Wrapper(InventoryExtension delegate) {
            this.delegate = delegate;
        }
        
        public static class Thaumcraft {
            public static boolean isCraftingSlot(Slot slot) {
                return slot instanceof SlotCraftingArcaneWorkbench;
            }
        }
        
        public static class ArsMagica {
            public static boolean isCraftingSlot(Slot slot) {
                return slot instanceof SlotMagiciansWorkbenchCrafting;
            }
        }
        
        public static class IronBackpacks {
            public static void getInventories(List<IItemHandler> inventories, EntityPlayer player) {
                PlayerWearingBackpackCapabilities backpackCapability = IronBackpacksCapabilities.getWearingBackpackCapability(player);
                if (backpackCapability != null) {
                    ItemStack backpackStack = backpackCapability.getEquippedBackpack();
                    if (backpackStack != null) {
                        inventories.add(new InvWrapper(new InventoryBackpack(backpackStack, true)));
                    }
                }
            }

            public static void getInventories(List<IItemHandler> inventories, @Nonnull ItemStack itemStack) {
                // Check if this is a backpack itemstack from the iron backpacks mod
                if (ModState.isIronBackpacksLoaded && itemStack.getItem() instanceof ItemBackpack) {
                    inventories.add(new InvWrapper(new InventoryBackpack(itemStack, true)));
                }
            }

            public static void saveIronBackpackNbtToItem(IItemHandler inventory, ItemStack itemStack) {
                // Workaround to prevent NPE in InventoryBackpack.writeToNBT due to null player object
                // We only set the "Items" nbt tag and assume the rest are saved at some other time
                NBTTagCompound nbt = itemStack.getTagCompound();
                if (nbt == null) {
                    nbt = new NBTTagCompound();
                    itemStack.setTagCompound(nbt);
                }
                NBTTagList inventoryNbt = new NBTTagList();
                nbt.setTag("Items", inventoryNbt);
                
                int slots = inventory.getSlots();
                for (int i = 0; i < slots; i++) {
                    ItemStack item = inventory.getStackInSlot(i);
                    if (!InventoryUtil.isEmptyItemStack(item)) {
                        NBTTagCompound itemNbt = new NBTTagCompound();
                        itemNbt.setByte("Slot", (byte)i);
                        item.writeToNBT(itemNbt);
                        inventoryNbt.appendTag(itemNbt);
                    }
                }
            }
            
            public static void maybeSaveIronBackpackNbt(IItemHandler inventory) {
                // Check if this inventory is a backpack item from IronBackpacks, and if so update the item NBT
                if (inventory.getClass() == InvWrapper.class) {
                    IInventory iInventory = ((InvWrapper)inventory).getInv();
                    if (iInventory instanceof InventoryBackpack) {
                        ItemStack backpackStack = ((InventoryBackpack)iInventory).getBackpackStack();
                        saveIronBackpackNbtToItem(inventory, backpackStack);
                    }
                }
            }
        }
        
        public static class ProjectE {
            @Nonnull
            public static void getInventories(List<IItemHandler> inventories, @Nonnull EntityPlayer player) {
                IAlchBagProvider alchBags = player.getCapability(ProjectEAPI.ALCH_BAG_CAPABILITY, null);
                if (alchBags != null) {
                    for (EnumDyeColor dyeColor : EnumSet.allOf(EnumDyeColor.class)) {
                        inventories.add(alchBags.getBag(dyeColor));
                    }
                }
            }
        }
        
        @Override
        public boolean isCraftingSlot(Slot slot) {
            if (delegate.isCraftingSlot(slot)) {
                return true;
            }
            if (ModState.isThaumcraftLoaded && Thaumcraft.isCraftingSlot(slot)) {
                return true;
            }
            if (ModState.isArsMagicaLoaded && ArsMagica.isCraftingSlot(slot)) {
                return true;
            }
            
            return false;
        }
        
        @Override
        public List<IItemHandler> getLocalInventories(@Nonnull EntityPlayer player) {
            List<IItemHandler> inventories = delegate.getLocalInventories(player);
            
            // Equipped backpack inventory
            if (ModState.isIronBackpacksLoaded) {
                IronBackpacks.getInventories(inventories, player);
            }
            
            return inventories;
        }

        @Override
        public List<IItemHandler> getInventories(@Nonnull ItemStack itemStack) {
            List<IItemHandler> inventories = delegate.getInventories(itemStack);
            
            if (ModState.isIronBackpacksLoaded) {
                IronBackpacks.getInventories(inventories, itemStack);
            }
            
            return inventories;
        }
        
        @Override
        public List<IItemHandler> getInventories(@Nonnull EntityPlayer player) {
            List<IItemHandler> inventories = delegate.getInventories(player);
            
            // Alchemical bag inventories
            if (ModState.isProjectELoaded) {
                ProjectE.getInventories(inventories, player);
            }
            // Equipped backpack inventory
            if (ModState.isIronBackpacksLoaded) {
                IronBackpacks.getInventories(inventories, player);
            }
            
            return inventories;
        }
        
        @Override
        public boolean forEachItemRecursive(IItemHandler inventory, ItemFunc itemFunc, int recursionDepth) {
            boolean changed = delegate.forEachItemRecursive(inventory, itemFunc, recursionDepth);
            
            if (changed) {
                if (ModState.isIronBackpacksLoaded) {
                    IronBackpacks.maybeSaveIronBackpackNbt(inventory);
                }
            }
            
            return changed;
        }
    }
    
    public void preInit(FMLPreInitializationEvent event) {
        InventoryExtension.INSTANCE = new Wrapper(InventoryExtension.INSTANCE);
    }
}
