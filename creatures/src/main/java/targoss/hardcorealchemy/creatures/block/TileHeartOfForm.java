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

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import targoss.hardcorealchemy.creatures.listener.ListenerWorldHumanity;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.Serialization;

public class TileHeartOfForm extends TileEntity {
    @CapabilityInject(IItemHandler.class)
    public static final Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = null;
    public static final int SLOT_MORPH_TARGET = 0;
    public static final int SLOT_TRUE_FORM    = 1;
    public static final int SLOT_COUNT        = 2;
    protected static final float MIN_ACTIVATION_DISTANCE = 1.0F; 
    
    public final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT);
    public UUID owner = null;
    
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == ITEM_HANDLER_CAPABILITY) {
            return (T)inventory;
        }
        return super.getCapability(capability, facing);
    }

    // TODO: A spark is activated with flint and steel
    // TODO: A spark is deactivated with water, or by breaking it
    // TODO: Removing the Seal of True Form from the spark will also deactivate it
    // TODO: Removing the non-human seal of form from the spark does nothing
    // TODO: If the heart becomes inactive for some reason (is deactivated, owner dies, loses their humanity, uses a seal of true form), then we need to update a world capability to set the heart to no longer active, and/or store a queued message to update the player capability
    // TODO: If the heart becomes inactive, and the owner is still alive, update their humanity to no longer be affected by the spark
    // TODO: How to check if the inventory has changed?
    // TODO: Capability to check if the heart is active (or we might just use a BlockState for that)
    // TODO: Syncing?

    public TileHeartOfForm(World world) {
        setWorld(world);
    }
    
    // TODO: Reference from BlockHeartOfForm (on neighbor change?)
    public void onIgnite(World world, BlockPos pos) {
        // TODO: The better the fuel source, the larger the activation distance (up to some reasonable max). Coal should give a reasonable default
        AxisAlignedBB bb = new AxisAlignedBB(pos).expandXyz(MIN_ACTIVATION_DISTANCE);
        List<EntityPlayer> nearbyPlayers = world.getEntitiesWithinAABB(EntityPlayer.class, bb, ListenerWorldHumanity.NoSparkPredicate.INSTANCE);
        if (!nearbyPlayers.isEmpty()) {
            EntityPlayer nearestPlayer = nearbyPlayers.get(0);
            double nearestDistanceSq = pos.distanceSq(nearestPlayer.posX, nearestPlayer.posY, nearestPlayer.posZ);
            for (int i = 1; i < nearbyPlayers.size(); ++i) {
                EntityPlayer player = nearbyPlayers.get(i);
                double distanceSq = pos.distanceSq(player.posX, player.posY, player.posZ);
                if (distanceSq < nearestDistanceSq) {
                    nearestPlayer = player;
                    nearestDistanceSq = distanceSq;
                }
            }
            ListenerWorldHumanity.onPlayerSparkCreated(nearestPlayer);
        }
        // TODO: Extinguish flame and play sound (maybe do this in BlockHeartOfForm)
    }
    
    public void breakBlock(World world, BlockPos pos) {
        if (owner != null) {
            ListenerWorldHumanity.onPlayerSparkBroken(owner);
            owner = null;
        }
        final int n = inventory.getSlots();
        for (int i = 0; i < n; ++i) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (!InventoryUtil.isEmptyItemStack(itemStack)) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemStack);
            }
        }
    }
    
    protected static final String NBT_OWNER = "owner";
    protected static final String NBT_INVENTORY = "inventory";
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound nbtOut = super.writeToNBT(compound);
        
        nbtOut.setTag(NBT_INVENTORY, inventory.serializeNBT());
        if (owner != null) {
            nbtOut.setString(NBT_OWNER, owner.toString());
        }
        
        return nbtOut;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        
        if (compound.hasKey(NBT_INVENTORY, Serialization.NBT_COMPOUND_ID)) {
            NBTTagCompound inventoryNBT = compound.getCompoundTag(NBT_INVENTORY);
            inventory.deserializeNBT(inventoryNBT);
        }
        if (compound.hasKey(NBT_OWNER, Serialization.NBT_STRING_ID)) {
            owner = UUID.fromString(compound.getString(NBT_OWNER));
        }
    }
}
