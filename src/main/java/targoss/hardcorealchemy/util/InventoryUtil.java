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
import javax.annotation.Nullable;

import am2.container.slot.SlotMagiciansWorkbenchCrafting;
import gr8pefish.ironbackpacks.capabilities.IronBackpacksCapabilities;
import gr8pefish.ironbackpacks.capabilities.player.PlayerWearingBackpackCapabilities;
import gr8pefish.ironbackpacks.container.backpack.InventoryBackpack;
import gr8pefish.ironbackpacks.items.backpacks.ItemBackpack;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.event.EventDrawInventoryItem;
import targoss.hardcorealchemy.event.EventTakeStack;
import thaumcraft.common.container.slot.SlotCraftingArcaneWorkbench;
import toughasnails.api.thirst.IDrink;

public class InventoryUtil {
    public static final ItemStack ITEM_STACK_EMPTY = null;

    public static boolean isEmptyItemStack(ItemStack itemStack) {
        return itemStack == null;
    }

    public static boolean isEmptyFluidStack(FluidStack fluidStack) {
        return fluidStack == null;
    }
    
    /**
     * Are items same in terms of item, meta, tagCompound (but not stack size)
     */
    public static boolean areItemsSameType(ItemStack a, ItemStack b) {
        if (a == b) {
            return true;
        }
        
        // Compare emptiness
        boolean aIsEmpty = isEmptyItemStack(a);
        boolean bIsEmpty = isEmptyItemStack(b);
        if (aIsEmpty != bIsEmpty) {
            return false;
        }
        if (aIsEmpty) {
            return true;
        }
        
        // Compare meta
        if (a.getItem() != b.getItem()) {
            return false;
        }
        if (a.getItemDamage() != b.getItemDamage()) {
            return false;
        }
        
        // Compare compound tags
        boolean aIsCompound = a.hasTagCompound();
        boolean bIsCompound = b.hasTagCompound();
        if (aIsCompound != bIsCompound) {
            return false;
        }
        if (aIsCompound && !a.getTagCompound().equals(b.getTagCompound())) {
            return false;
        }
        
        return true;
    }

    /**
     * Check if the slot is a crafting table output slot.
     * This function also handles "technical" slots used by EventDrawInventoryItem.
     */
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

    /**
     * Check if the slot is a slot containing an item the player can take and/or place,
     * as opposed to some documentation/display slot.
     * Player field is required and determines which player is about to interact with the slot.
     * This function also handles "technical" slots used by EventDrawInventoryItem.
     */
    public static boolean isInteractableSlot(Slot slot, EntityPlayer player) {
        // Special cases
        if (slot == null) {
            return false;
        }
        if (slot == EventDrawInventoryItem.MOUSE_SLOT) {
            return true;
        }
        // The best way to check if an item is actually in a real inventory slot,
        // as opposed to some form of documentation or display, is to check
        // if the player can take the item.
        // However, we added an event which can stop this from happening
        // in a real inventory slot, so we need to check for that case
        // There is also a possible case where the event is canceled yet the
        // slot isn't real, which would cause this heuristic to fail.
        // For now, this is enough.
        EventTakeStack.Pre stackEvent = new EventTakeStack.Pre(slot, player);
        // Can take stack, or was taking stack canceled?
        return (slot.canTakeStack(player) || MinecraftForge.EVENT_BUS.post(stackEvent));
    }
    
    @SideOnly(Side.CLIENT)
    public static Slot getSlotUnderMouse() {
        GuiScreen guiScreen = Minecraft.getMinecraft().currentScreen;
        if (guiScreen instanceof GuiContainer) {
            return ((GuiContainer)guiScreen).getSlotUnderMouse();
        }
        return null;
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
    
    public static int getArmorInventorySize(EntityPlayer player) {
        return player.inventory.armorInventory.length;
    }
    
    public static ItemStack getArmorStackInSlot(EntityPlayer player, int armorSlot) {
        int armorInventoryOffset = player.inventory.mainInventory.length;
        int slot = armorInventoryOffset + armorSlot;
        return player.inventory.getStackInSlot(slot);
    }
    
    public static void setArmorStackInSlot(EntityPlayer player, int armorSlot, ItemStack itemStack) {
        int armorInventoryOffset = player.inventory.mainInventory.length;
        int slot = armorInventoryOffset + armorSlot;
        player.inventory.setInventorySlotContents(slot, itemStack);
    }
    
    public static interface ItemFunc {
        /**
         * Return true if the inventory changed
         */
        boolean apply(IItemHandler inventory, int slot, ItemStack itemStack);
    }
    
    public static final int DEFAULT_INVENTORY_RECURSION_DEPTH = 6;

    /**
     * Return true if the inventory changed
     */
    public static boolean forEachItemRecursive(IItemHandler inventory, ItemFunc itemFunc, int recursionDepth) {
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

            for (IItemHandler inventoryStack : InventoryUtil.getInventories(itemStack)) {
                if (inventory == null) {
                    continue;
                }
                changed |= forEachItemRecursive(inventoryStack, itemFunc, recursionDepth - 1);
            }
            
        }
        
        if (changed) {
            // Check if this inventory is a backpack item from IronBackpacks, and if so update the item NBT
            if (inventory.getClass() == InvWrapper.class) {
                IInventory iInventory = ((InvWrapper)inventory).getInv();
                if (ModState.isIronBackpacksLoaded && iInventory instanceof InventoryBackpack) {
                    ItemStack backpackStack = ((InventoryBackpack)iInventory).getBackpackStack();
                    InventoryUtil.saveIronBackpackNbt(inventory, backpackStack);
                }
            }
        }
        
        return changed;
    }
    
    /**
     * Return true if the inventory changed
     */
    public static boolean forEachItemRecursive(IItemHandler inventory, ItemFunc itemFunc) {
        return forEachItemRecursive(inventory, itemFunc, DEFAULT_INVENTORY_RECURSION_DEPTH);
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
            if (!InventoryUtil.isEmptyItemStack(item)) {
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
    
    @Optional.Method(modid = ModState.TAN_ID)
    public static boolean isTANDrink(Item item) {
        return item instanceof IDrink ||
                item.getRegistryName().getResourcePath().equals("canteen");
    }

    /** Get the material for swords, tools, and armor */
    public static ItemStack getMaterialStack(ItemStack itemStack) {
        if (isEmptyItemStack(itemStack)) {
            return ITEM_STACK_EMPTY;
        }
        Item item = itemStack.getItem();
        if (item instanceof ItemSword) {
            String materialName =  ((ItemSword)item).getToolMaterialName();
            Item.ToolMaterial material = Item.ToolMaterial.valueOf(materialName);
            return material.getRepairItemStack();
        }
        if (item instanceof ItemTool) {
            return ((ItemTool)item).getToolMaterial().getRepairItemStack();
        }
        if (item instanceof ItemArmor) {
            Item materialItem = ((ItemArmor)item).getArmorMaterial().getRepairItem();
            return new ItemStack(materialItem);
        }
        return ITEM_STACK_EMPTY;
    }
    
    /** Get the material name for swords, tools, and armor.
     *  Block materials don't reflect the underlying ingredients,
     *  so are not checked here. */
    public static @Nullable String getMaterialName(ItemStack itemStack) {
        if (isEmptyItemStack(itemStack)) {
            return null;
        }
        Item item = itemStack.getItem();
        if (item instanceof ItemSword) {
            return ((ItemSword)item).getToolMaterialName();
        }
        if (item instanceof ItemTool) {
            return ((ItemTool)item).getToolMaterial().name();
        }
        if (item instanceof ItemArmor) {
            return ((ItemArmor)item).getArmorMaterial().name();
        }
        return null;
    }
}
