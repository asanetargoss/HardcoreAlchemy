/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Magic.
 *
 * Hardcore Alchemy Magic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Magic is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Magic. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.magic.listener;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;

import am2.container.slot.SlotMagiciansWorkbenchCrafting;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.items.IItemHandler;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.IInventoryExtension;
import targoss.hardcorealchemy.util.InventoryExtension;
import targoss.hardcorealchemy.util.InventoryUtil.ItemFunc;
import thaumcraft.common.container.slot.SlotCraftingArcaneWorkbench;

public class ListenerInventoryExtension extends HardcoreAlchemyListener {
    public static class Wrapper implements IInventoryExtension {
        public IInventoryExtension delegate;
        
        public Wrapper(IInventoryExtension delegate) {
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
            return delegate.getLocalInventories(player);
        }

        @Override
        public List<IItemHandler> getInventories(@Nonnull ItemStack itemStack) {
            return delegate.getInventories(itemStack);
        }
        
        @Override
        public List<IItemHandler> getInventories(@Nonnull EntityPlayer player) {
            List<IItemHandler> inventories = delegate.getInventories(player);
            
            // Alchemical bag inventories
            if (ModState.isProjectELoaded) {
                ProjectE.getInventories(inventories, player);
            }
            
            return inventories;
        }
        
        @Override
        public boolean forEachItemRecursive(IItemHandler inventory, ItemFunc itemFunc, int recursionDepth) {
            return delegate.forEachItemRecursive(inventory, itemFunc, recursionDepth);
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
