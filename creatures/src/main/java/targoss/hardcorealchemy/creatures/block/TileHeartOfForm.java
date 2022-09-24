/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures.block;

import net.minecraft.nbt.NBTBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

public class TileHeartOfForm extends TileEntity {
    @CapabilityInject(IItemHandler.class)
    public static final Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = null;
    public static final ResourceLocation ITEM_HANDLER_RESOURCE = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "item_handler");
    public static final int SLOT_MORPH_TARGET = 0;
    public static final int SLOT_TRUE_FORM    = 1;
    public static final int SLOT_COUNT        = 2;
    
    // TODO: If the heart becomes inactive for some reason and the owner is not present (owner dies, loses their humanity, uses a seal of true form), then we need to update a world capability to set the heart to no longer active.
    // TODO: An active spark detects a seal missing/deactivation event (seal is missing from a slot, form seal is changed, spark extinguished, block broken) and then checks if the owner is nearby. If the owner is nearby, nothing bad happens. If not, the spark attempts to take the knowledge of seals lost from players within interaction range, morphing them into other forms as needed. The player that loses their humanity gets the extended humanity decay time as if bound to a heart of form. If the heart is unable to erase forms from suitable players, it kills the nearest player. If no player is nearby, the owner is killed. In most cases, this gives the interacting player the choice to sacrifice their humanity to save the humanity of another, or a way to sabotage the other player. Ideally, a player that forgot the respective form (doesn't have to be the same player, but can't be the owner) could place the correct seal in the correct empty slot to get their form back
    // TODO: How to check if the inventory has changed?
    // TODO: Capability to store the owner and bound morph (seals remain in the slots)
    // TODO: Capability to check if the heart is active (or we might just use a BlockState for that)
    // TODO: Syncing?
    
    public static class ItemHandlerProvider implements ICapabilitySerializable<NBTBase> {
        IItemHandler instance;
        
        public ItemHandlerProvider() {
            instance = new ItemStackHandler(SLOT_COUNT);
        }

        @Override
        public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
            return capability == ITEM_HANDLER_CAPABILITY;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
            if (capability == ITEM_HANDLER_CAPABILITY) {
                return (T)instance;
            }
            return null;
        }

        @Override
        public NBTBase serializeNBT() {
            return ITEM_HANDLER_CAPABILITY.getStorage().writeNBT(ITEM_HANDLER_CAPABILITY, instance, null);
        }

        @Override
        public void deserializeNBT(NBTBase nbt) {
            ITEM_HANDLER_CAPABILITY.getStorage().readNBT(ITEM_HANDLER_CAPABILITY, instance, null, nbt);
        }
        
    }

    public TileHeartOfForm(World world) {
        setWorld(world);
    }
}
