/*
 * Copyright 2018 asanetargoss
 * 
 * This file is part of Hardcore Alchemy.
 * 
 * Hardcore Alchemy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 * 
 * Hardcore Alchemy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hardcore Alchemy.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.util;

import java.util.ArrayList;
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
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import targoss.hardcorealchemy.ModState;
import thaumcraft.common.container.slot.SlotCraftingArcaneWorkbench;

public class InventoryUtil {
    public static boolean isCraftingSlot(Slot slot) {
        if (slot instanceof SlotCrafting) {
            return true;
        }
        if (ModState.isThaumcraftLoaded && isThaumcraftCraftingSlot(slot)) {
            return true;
        }
        if (ModState.isArsMagicaLoaded && isArsMagicaCraftingSlot(slot)) {
            return true;
        }
        
        return false;
    }

    @Optional.Method(modid=ModState.THAUMCRAFT_ID)
    private static boolean isThaumcraftCraftingSlot(Slot slot) {
        return slot instanceof SlotCraftingArcaneWorkbench;
    }
    
    @Optional.Method(modid=ModState.ARS_MAGICA_ID)
    private static boolean isArsMagicaCraftingSlot(Slot slot) {
        return slot instanceof SlotMagiciansWorkbenchCrafting;
    }

    @Nonnull
    public static List<IItemHandler> getInventories(@Nonnull ItemStack itemStack) {
        List<IItemHandler> inventories = new ArrayList<>();
        
        // Check if the stack has the item handler capability
        {
            IItemHandler inventory = itemStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (inventory != null) {
                inventories.add(inventory);
            }
        }
        // Check if this is a backpack itemstack from the iron backpacks mod
        if (ModState.isIronBackpacksLoaded && itemStack.getItem() instanceof ItemBackpack) {
            inventories.add(new InvWrapper(new InventoryBackpack(itemStack, true)));
        }
        
        return inventories;
    }

    @Nonnull
    public static List<IItemHandler> getInventories(@Nonnull TileEntity tileEntity) {
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

    @Nonnull
    public static List<IItemHandler> getInventories(@Nonnull EntityPlayer player) {
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
        // Alchemical bag inventories
        if (ModState.isProjectELoaded) {
            inventories.addAll(getAlchemicalBags(player));
        }
        // Equipped backpack inventory
        if (ModState.isIronBackpacksLoaded) {
            PlayerWearingBackpackCapabilities backpackCapability = IronBackpacksCapabilities.getWearingBackpackCapability(player);
            if (backpackCapability != null) {
                ItemStack backpackStack = backpackCapability.getEquippedBackpack();
                if (backpackStack != null) {
                    inventories.add(new InvWrapper(new InventoryBackpack(backpackStack, true)));
                }
            }
        }
        
        return inventories;
    }

    @Optional.Method(modid = ModState.IRON_BACKPACKS_ID)
    public static void saveIronBackpackNbt(IItemHandler inventory, ItemStack itemStack) {
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
            if (!MiscVanilla.isEmptyItemStack(item)) {
                NBTTagCompound itemNbt = new NBTTagCompound();
                itemNbt.setByte("Slot", (byte)i);
                item.writeToNBT(itemNbt);
                inventoryNbt.appendTag(itemNbt);
            }
        }
    }

    @Nonnull
    @Optional.Method(modid = ModState.PROJECT_E_ID)
    public static List<IItemHandler> getAlchemicalBags(@Nonnull EntityPlayer player) {
        List<IItemHandler> inventories = new ArrayList<>();
        
        IAlchBagProvider alchBags = player.getCapability(ProjectEAPI.ALCH_BAG_CAPABILITY, null);
        if (alchBags != null) {
            for (EnumDyeColor dyeColor : EnumSet.allOf(EnumDyeColor.class)) {
                inventories.add(alchBags.getBag(dyeColor));
            }
        }
        
        return inventories;
    }
}
