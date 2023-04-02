/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import targoss.hardcorealchemy.coremod.ObfuscatedName;
import targoss.hardcorealchemy.event.EventDrawInventoryItem;
import targoss.hardcorealchemy.event.EventTakeStack;

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
    
    public static boolean isHotbarSlotIndex(int index) {
        return InventoryPlayer.isHotbar(index);
    }
    
    public static List<Slot> getPlayerHotbarSlots(InventoryPlayer inv) {
        List<Slot> slots = new ArrayList<>();
        final int n = InventoryPlayer.getHotbarSize();
        for (int i = 0; i < n; ++i) {
            slots.add(new Slot(inv, i, 0, 0));
        }
        return slots;
    }
    
    public static int getInsertionSlot(InventoryPlayer inventoryPlayer, ItemStack itemStack) {
        int existing = inventoryPlayer.storeItemStack(itemStack);
        if (existing != -1) {
            return existing;
        }
        return inventoryPlayer.getFirstEmptyStack();
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
    
    protected static ObfuscatedName ON_ITEM_USE_FINISH = new ObfuscatedName("func_77654_b" /*onItemUseFinish*/);
    protected static Map<Item, Boolean> hasHoldRightClick = new HashMap<>();
    /** Currently, we assume this function is only called on the client side, so we deliberately choose a very small cache. */
    protected static final int MAX_HOLD_RIGHT_CLICK_CACHE = 16;
    public static boolean isHoldRightClickItem(Item item) {
        Boolean has = hasHoldRightClick.get(item);
        if (has != null) {
            return has;
        }
        // Clear the cache from time to time so it doesn't get too big
        if (hasHoldRightClick.size() >= MAX_HOLD_RIGHT_CLICK_CACHE) {
            hasHoldRightClick.clear();
        }
        // An item is considered to have hold-right-click functionality when it overrides Item::onItemUseFinish(...)
        has = InvokeUtil.hasPrivateMethod(false, item.getClass(), Item.class, ON_ITEM_USE_FINISH.get(), ItemStack.class, World.class, EntityLivingBase.class);
        hasHoldRightClick.put(item, has);
        return has;
    }
}
