/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Capstone.
 *
 * Hardcore Alchemy Capstone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * Hardcore Alchemy Capstone is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Hardcore Alchemy Capstone.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.capstone.listener;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import gr8pefish.ironbackpacks.capabilities.IronBackpacksCapabilities;
import gr8pefish.ironbackpacks.capabilities.player.PlayerWearingBackpackCapabilities;
import gr8pefish.ironbackpacks.container.backpack.InventoryBackpack;
import gr8pefish.ironbackpacks.items.backpacks.ItemBackpack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import targoss.hardcorealchemy.capstone.CapstoneModState;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.IInventoryExtension;
import targoss.hardcorealchemy.util.InventoryExtension;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.InventoryUtil.ItemFunc;

public class ListenerInventoryExtension extends HardcoreAlchemyListener {
    public static class Wrapper implements IInventoryExtension {
        public IInventoryExtension delegate;
        
        public Wrapper(IInventoryExtension delegate) {
            this.delegate = delegate;
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
                if (CapstoneModState.isIronBackpacksLoaded && itemStack.getItem() instanceof ItemBackpack) {
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
        
        @Override
        public boolean isCraftingSlot(Slot slot) {
            return delegate.isCraftingSlot(slot);
        }
        
        @Override
        public List<IItemHandler> getLocalInventories(@Nonnull EntityPlayer player) {
            List<IItemHandler> inventories = delegate.getLocalInventories(player);
            
            // Equipped backpack inventory
            if (CapstoneModState.isIronBackpacksLoaded) {
                IronBackpacks.getInventories(inventories, player);
            }
            
            return inventories;
        }

        @Override
        public List<IItemHandler> getInventories(@Nonnull ItemStack itemStack) {
            List<IItemHandler> inventories = delegate.getInventories(itemStack);
            
            if (CapstoneModState.isIronBackpacksLoaded) {
                IronBackpacks.getInventories(inventories, itemStack);
            }
            
            return inventories;
        }
        
        @Override
        public List<IItemHandler> getInventories(@Nonnull EntityPlayer player) {
            List<IItemHandler> inventories = delegate.getInventories(player);
            
            // Equipped backpack inventory
            if (CapstoneModState.isIronBackpacksLoaded) {
                IronBackpacks.getInventories(inventories, player);
            }
            
            return inventories;
        }
        
        @Override
        public boolean forEachItemRecursive(IItemHandler inventory, ItemFunc itemFunc, int recursionDepth) {
            boolean changed = delegate.forEachItemRecursive(inventory, itemFunc, recursionDepth);
            
            if (changed) {
                if (CapstoneModState.isIronBackpacksLoaded) {
                    IronBackpacks.maybeSaveIronBackpackNbt(inventory);
                }
            }
            
            return changed;
        }

        @Override
        public List<IItemHandler> getLocalInventories(Entity entity) {
            return delegate.getLocalInventories(entity);
        }

        @Override
        public List<IItemHandler> getInventories(TileEntity tileEntity) {
            return delegate.getInventories(tileEntity);
        }

        @Override
        public List<IItemHandler> getInventories(Entity entity) {
            return delegate.getInventories(entity);
        }

        @Override
        public boolean forEachItemRecursive(IItemHandler inventory, ItemFunc itemFunc) {
            return delegate.forEachItemRecursive(inventory, itemFunc);
        }

        @Override
        public boolean forEachItemRecursive(Collection<IItemHandler> inventories, ItemFunc itemFunc) {
            return delegate.forEachItemRecursive(inventories, itemFunc);
        }
    }
    
    public void preInit(FMLPreInitializationEvent event) {
        InventoryExtension.INSTANCE = new Wrapper(InventoryExtension.INSTANCE);
    }
}
