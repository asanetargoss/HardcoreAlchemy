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

import static targoss.hardcorealchemy.creatures.item.Items.SEAL_OF_FORM;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import targoss.hardcorealchemy.capability.UniverseCapabilityManager;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ProviderHumanity;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.creatures.HardcoreAlchemyCreatures;
import targoss.hardcorealchemy.creatures.capability.worldhumanity.ICapabilityWorldHumanity;
import targoss.hardcorealchemy.creatures.capability.worldhumanity.ICapabilityWorldHumanity.State;
import targoss.hardcorealchemy.creatures.event.EventHumanityPhylactery;
import targoss.hardcorealchemy.creatures.item.ItemSealOfForm;
import targoss.hardcorealchemy.creatures.listener.ListenerWorldHumanity;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.Serialization;
import targoss.hardcorealchemy.util.WorldUtil;

public class TileHumanityPhylactery extends TileEntity {
    // TODO: Add a default constructor to appease net.minecraft.tileentity.TileEntity.create
    
    @CapabilityInject(IItemHandler.class)
    public static final Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = null;
    public static final int SLOT_MORPH_TARGET = 0;
    public static final int SLOT_TRUE_FORM    = 1;
    public static final int SLOT_FUEL         = 2;
    public static final int SLOT_COUNT        = 3;
    protected static final int MIN_FUEL_QUALITY = TileEntityFurnace.getItemBurnTime(new ItemStack(Items.COAL, 1, 0)); // 0 = regular coal (although burn time should be the same for charcoal)
    /** Distance from the surface of the block */
    protected static final int MIN_ACTIVATION_DISTANCE = 3;
    protected static final float FUEL_QUALITY_FACTOR = 4.0F;
    protected static final float DISTANCE_MAGNITUDE = (float)Math.log(2.0);
    protected static final int MAX_ACTIVATION_DISTANCE = 48;

    protected boolean sideEffects = true;
    
    @CapabilityInject(ICapabilityMisc.class)
    public static final Capability<ICapabilityMisc> MISC_CAPABILITY = null;
    @CapabilityInject(ICapabilityWorldHumanity.class)
    public static final Capability<ICapabilityWorldHumanity> HUMANITY_WORLD_CAPABILITY = null;
    
    protected class Inventory extends ItemStackHandler {
        protected boolean sideEffects = true;
        
        public Inventory(int slotCount) {
            super(slotCount);
        }
        
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            switch (slot) {
            case SLOT_MORPH_TARGET:
                if (getMorphTarget(stack) == null) {
                    return stack;
                }
                break;
            case SLOT_TRUE_FORM:
                if (!isTrueFormSeal(stack)) {
                    return stack;
                }
                break;
            case SLOT_FUEL:
                if (!isSufficientFuel(stack)) {
                    return stack;
                }
                break;
            default:
                return stack;
            }
            return super.insertItem(slot, stack, simulate);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (!sideEffects) {
                sideEffects = true;
                return;
            }
            if (tryDisassemble()) {
                return;
            }
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (tryDouse(world, pos, pos.offset(facing))) {
                    return;
                }
            }
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (tryIgnite(world, pos, pos.offset(facing))) {
                    return;
                }
            }
        }
        
        protected Inventory withoutSideEffects() {
            this.sideEffects = false;
            return this;
        }
    }
    
    public static class NoForgotMorphPredicate implements Predicate<EntityPlayer> {
        public static final NoForgotMorphPredicate INSTANCE = new NoForgotMorphPredicate();
        
        @Override
        public boolean apply(EntityPlayer player) {
            ICapabilityHumanity humanity = player.getCapability(ProviderHumanity.HUMANITY_CAPABILITY, null);
            if (humanity == null) {
                return false;
            }
            if (!humanity.shouldDisplayHumanity()) {
                return false;
            }
            return !humanity.getHasForgottenMorphAbility();
        }
    }

    public static class ForgotMorphPredicate implements Predicate<EntityPlayer> {
        public static final ForgotMorphPredicate INSTANCE = new ForgotMorphPredicate();
        
        @Override
        public boolean apply(EntityPlayer player) {
            ICapabilityHumanity humanity = player.getCapability(ProviderHumanity.HUMANITY_CAPABILITY, null);
            if (humanity == null) {
                return false;
            }
            if (!humanity.shouldDisplayHumanity()) {
                return false;
            }
            return humanity.getHasForgottenMorphAbility();
        }
    }

    public final Inventory inventory = new Inventory(SLOT_COUNT);
    public UUID playerUUID = null;
    public UUID lifetimeUUID = null;
    // TODO: Dormant phylacteries are technically still active, but don't have a flame, and cannot be doused with water
    public boolean dormant = false;
    
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

    // TODO: Syncing?

    public TileHumanityPhylactery(World world) {
        setWorld(world);
    }
    
    public static void checkWorldState(@Nullable TileHumanityPhylactery phyTE, ICapabilityWorldHumanity.Phylactery oldPhy, UUID newPlayerLifetimeUUID, UUID newPlayerUUID) {
        checkWorldState(phyTE, oldPhy, newPlayerLifetimeUUID, newPlayerUUID, false);
    }
    
    protected static void checkWorldState(@Nullable TileHumanityPhylactery phyTE, ICapabilityWorldHumanity.Phylactery oldPhy, UUID newPlayerLifetimeUUID, UUID newPlayerUUID, boolean isWorldLoad) {
        assert(oldPhy != null);
        
        if (phyTE == null) {
            assert(newPlayerLifetimeUUID != null);
            assert(newPlayerUUID != null);
            
            World phyWorld = WorldUtil.maybeGetDimWorld(oldPhy.dimension);
            if (phyWorld != null && phyWorld.isBlockLoaded(oldPhy.pos)) {
                TileEntity te = phyWorld.getTileEntity(oldPhy.pos);
                if (te instanceof TileHumanityPhylactery) {
                    phyTE = (TileHumanityPhylactery)te;
                }
                else {
                    HardcoreAlchemyCreatures.LOGGER.warn("Expected tile entity at pos: " + oldPhy.pos + ", dim: " + oldPhy.dimension);
                    return;
                }
            }
        }
        else {
            if (newPlayerLifetimeUUID == null) {
                // It's not possible to activate a humanity phylactery unless it is loaded, so it's safe to assume nothing will happen
                return;
            }
        }
        
        ICapabilityWorldHumanity.Phylactery newPhy = ListenerWorldHumanity.getBlockPhylactery(newPlayerLifetimeUUID, newPlayerUUID, oldPhy.pos, oldPhy.dimension);
        boolean shouldUpdate;
        boolean worldStateActive;
        boolean worldStateDormant;
        if (newPhy != null) {
            switch (newPhy.state) {
            case ACTIVE:
                shouldUpdate = true;
                worldStateActive = true;
                worldStateDormant = false;
                break;
            case DEACTIVATED:
                shouldUpdate = true;
                worldStateActive = false;
                worldStateDormant = false;
                break;
            case DORMANT:
                shouldUpdate = true;
                worldStateActive = true;
                worldStateDormant = true;
                break;
            default:
                // This state is invalid/not handled, so don't do anything
                shouldUpdate = false;
                worldStateActive = false;
                worldStateDormant = false;
                break;
            }
        }
        else {
            // Assume the phylactery is deactivated
            shouldUpdate = true;
            worldStateActive = false;
            worldStateDormant = false;
        }
        if (shouldUpdate) {
            if (worldStateActive) {
                create(phyTE, null, null, newPhy.morphTarget, oldPhy, newPhy, isWorldLoad);
            }
            else {
                deactivate(phyTE, oldPhy, isWorldLoad);
            }
            if (phyTE != null) {
                phyTE.setDormant(worldStateDormant);
            }
        }
    }
    
    public boolean isActive() {
        return playerUUID != null;
    }
    
    public boolean isDormant() {
        return dormant;
    }
    
    protected static void create(@Nullable TileHumanityPhylactery phyTE, @Nullable EntityPlayer player, @Nullable ICapabilityMisc misc, AbstractMorph morphTarget, @Nullable ICapabilityWorldHumanity.Phylactery oldPhy, ICapabilityWorldHumanity.Phylactery newPhy, boolean isWorldLoad) {
        if (phyTE != null) {
            phyTE.playerUUID = newPhy.playerUUID;
            phyTE.lifetimeUUID = newPhy.lifetimeUUID;
            if (!isWorldLoad) {
                assert(player != null);
                assert(misc != null);
                assert(morphTarget != null);
                MinecraftForge.EVENT_BUS.post(new EventHumanityPhylactery.Create(player, misc, morphTarget, phyTE.getWorld(), phyTE.getPos(), phyTE.getWorld().provider.getDimension()));
            }
        }
        else {
            if (oldPhy == null) {
                // Extrapolate the default value
                oldPhy = new ICapabilityWorldHumanity.Phylactery(null, null, newPhy.pos, newPhy.dimension, ICapabilityWorldHumanity.State.DEACTIVATED, null);
            }
            if (!isWorldLoad) {
                boolean ownerChanged = !Objects.equals(newPhy.playerUUID, oldPhy.playerUUID) || !Objects.equals(oldPhy.lifetimeUUID, newPhy.lifetimeUUID);
                boolean stateChanged = newPhy.state != oldPhy.state || !Objects.equals(newPhy.morphTarget, oldPhy.morphTarget);
                if (ownerChanged) {
                    MinecraftForge.EVENT_BUS.post(new EventHumanityPhylactery.Destroy(oldPhy.lifetimeUUID, oldPhy.playerUUID, oldPhy.pos, oldPhy.dimension));
                }
                if (ownerChanged || stateChanged) {
                    MinecraftForge.EVENT_BUS.post(new EventHumanityPhylactery.Recreate(newPhy.playerUUID, newPhy.lifetimeUUID, newPhy.morphTarget, newPhy.pos, newPhy.dimension));
                }
            }
        }
    }
    
    protected void activate(EntityPlayer player, ICapabilityMisc misc, AbstractMorph morphTarget, BlockPos pos, int dimension) {
        assert(!isActive());
        
        ICapabilityWorldHumanity worldHumanity = UniverseCapabilityManager.INSTANCE.getCapability(HUMANITY_WORLD_CAPABILITY);
        if (worldHumanity == null) {
            return;
        }
        
        ICapabilityWorldHumanity.Phylactery oldPhy = new ICapabilityWorldHumanity.Phylactery(this.lifetimeUUID, this.playerUUID, getPos(), getWorld().provider.getDimension(), ICapabilityWorldHumanity.State.DEACTIVATED, null);
        ICapabilityWorldHumanity.Phylactery newPhy = new ICapabilityWorldHumanity.Phylactery(misc.getLifetimeUUID(), player.getUniqueID(), getPos(), getWorld().provider.getDimension(), ICapabilityWorldHumanity.State.ACTIVE, morphTarget);
        worldHumanity.registerPhylactery(newPhy);
        create(this, player, misc, morphTarget, oldPhy, newPhy, false);
    }
    
    protected void setDeactivated() {
        this.playerUUID = null;
        this.lifetimeUUID = null;
        this.dormant = false;
    }
    
    protected static void deactivate(@Nullable TileHumanityPhylactery phyTE, ICapabilityWorldHumanity.Phylactery oldPhy, boolean isWorldLoad) {
        assert(oldPhy != null);
        
        if (!isWorldLoad && oldPhy.state != State.DEACTIVATED && oldPhy.state != State.DORMANT) {
            ICapabilityWorldHumanity worldHumanity = UniverseCapabilityManager.INSTANCE.getCapability(HUMANITY_WORLD_CAPABILITY);
            if (worldHumanity != null) {
                boolean wasRegistered = worldHumanity.unregisterPhylactery(oldPhy.lifetimeUUID, oldPhy.playerUUID, oldPhy.pos, oldPhy.dimension);
                if (wasRegistered) {
                    MinecraftForge.EVENT_BUS.post(new EventHumanityPhylactery.Destroy(oldPhy.lifetimeUUID, oldPhy.playerUUID, oldPhy.pos, oldPhy.dimension));
                }
            }
            
        }
        if (phyTE != null) {
            phyTE.setDeactivated();
        }
    }
    
    public void deactivate(boolean isWorldLoad) {
        ICapabilityWorldHumanity.Phylactery oldPhy = new ICapabilityWorldHumanity.Phylactery(lifetimeUUID, lifetimeUUID, this.getPos(), getWorld().provider.getDimension(), this.getPhylacteryState(), this.getMorphTarget());
        deactivate(this, oldPhy, isWorldLoad);
    }
    
    private State getPhylacteryState() {
        if (!isActive()) {
            return State.DEACTIVATED;
        }
        else if (isDormant()) {
            return State.DORMANT;
        }
        else {
            return State.ACTIVE;
        }
    }

    public void setDormant(boolean dormant) {
        this.dormant = dormant;
    }
    
    protected static boolean isSufficientFuel(ItemStack itemStack) {
        int burnTime = TileEntityFurnace.getItemBurnTime(itemStack);
        return burnTime >= MIN_FUEL_QUALITY;
    }
    
    public boolean hasSufficientFuel() {
        ItemStack itemStack = inventory.getStackInSlot(SLOT_FUEL);
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return false;
        }
        return isSufficientFuel(itemStack);
    }
    
    protected static AbstractMorph getMorphTarget(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item != SEAL_OF_FORM) {
            return null;
        }
        AbstractMorph morph = ItemSealOfForm.getEntityMorph(itemStack);
        return morph;
    }
    
    public AbstractMorph getMorphTarget() {
        ItemStack itemStack = inventory.getStackInSlot(SLOT_MORPH_TARGET);
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return null;
        }
        return getMorphTarget(itemStack);
    }
    
    protected int getActivationDistance() {
        assert(hasSufficientFuel());
        ItemStack itemStack = inventory.getStackInSlot(SLOT_FUEL);
        int burnTime = TileEntityFurnace.getItemBurnTime(itemStack);
        if (burnTime == MIN_FUEL_QUALITY) {
            return MIN_ACTIVATION_DISTANCE;
        }
        int calcDistance = (int)(MIN_ACTIVATION_DISTANCE + (FUEL_QUALITY_FACTOR * Math.log(burnTime - MIN_FUEL_QUALITY) / DISTANCE_MAGNITUDE ));
        return Math.min(calcDistance, MAX_ACTIVATION_DISTANCE);
    }
    
    protected static boolean isTrueFormSeal(ItemStack itemStack) {
        Item item = itemStack.getItem();
        if (item != SEAL_OF_FORM) {
            return false;
        }
        return ItemSealOfForm.hasHumanTag(itemStack);
    }
    
    protected boolean hasTrueFormSeal() {
        ItemStack itemStack = inventory.getStackInSlot(SLOT_TRUE_FORM);
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return false;
        }
        return isTrueFormSeal(itemStack);
    }

    /** Check for missing item. Deactivate the phylactery if conditions are met.
     * Return true if the caller should stop checking neighboring blocks. **/
    protected boolean tryDisassemble() {
        if (!this.sideEffects) {
            return true;
        }
        if (!isActive()) {
            return false;
        }
        ItemStack trueFormStack = inventory.getStackInSlot(SLOT_TRUE_FORM);
        if (InventoryUtil.isEmptyItemStack(trueFormStack) ||
                trueFormStack.getItem() != SEAL_OF_FORM ||
                !ItemSealOfForm.hasHumanTag(trueFormStack)) {
            deactivate(false);
            return true;
        }
        return false;
    }

    /** Check for nearby water (or other liquid satisfying Fluid.doesVaporize()). Put out all nearby fire and
     * deactivate the phylactery if conditions are met. Return true if the
     * caller should stop checking neighboring blocks. **/
    protected boolean tryDouse(World world, BlockPos pos, BlockPos waterTestPos) {
        if (!this.sideEffects) {
            return true;
        }
        if (isDormant()) {
            return true;
        }
        IFluidHandler fluidHandler = FluidUtil.getFluidHandler(world, pos, null);
        if (fluidHandler == null) {
            return false;
        }
        FluidStack testFluidStack = fluidHandler.drain(Integer.MAX_VALUE, false);
        Fluid fluid = testFluidStack.getFluid();
        if (!fluid.doesVaporize(testFluidStack)) {
            return false;
        }
        
        deactivate(false);
        // Put out all neighboring fire blocks to prevent an edge case where the tile doesn't activate when fire is nearby
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos fireTestPos = pos.offset(facing);
            if (fireTestPos.equals(waterTestPos)) {
                continue;
            }
            IBlockState fireTestBlockState = world.getBlockState(fireTestPos);
            Block fireTestBlock = fireTestBlockState.getBlock();
            if (fireTestBlock instanceof BlockFire) {
                this.sideEffects = false;
                world.setBlockState(fireTestPos, Blocks.AIR.getDefaultState());
                this.sideEffects = true;
            }
        }
        return true;
    }

    /** Check for nearby fire. Extinguish fire and activate the phylactery
     *  if conditions are met. Return true if the caller should stop
     *  checking neighboring blocks. **/
    protected boolean tryIgnite(World world, BlockPos pos, BlockPos testPos) {
        if (!this.sideEffects) {
            return true;
        }
        if (isActive()) {
            return true;
        }
        IBlockState testBlockState = world.getBlockState(testPos);
        Block testBlock = testBlockState.getBlock();
        if (!(testBlock instanceof BlockFire)) {
            return false;
        }
        if (!hasSufficientFuel()) {
            return true;
        }
        AbstractMorph morphTarget = getMorphTarget();
        if (morphTarget == null) {
            return true;
        }
        // The better the fuel source, the larger the activation distance (up to some reasonable max). Coal should give a reasonable default.
        final int activationDistance = getActivationDistance();
        AxisAlignedBB bb = new AxisAlignedBB(pos).expandXyz(activationDistance);
        boolean needTrueFormSeal = hasTrueFormSeal();
        // Target different player depending on if this tile's inventory has a seal of true form in it
        Predicate<EntityPlayer> predicate = needTrueFormSeal ? NoForgotMorphPredicate.INSTANCE : ForgotMorphPredicate.INSTANCE;
        List<EntityPlayer> nearbyPlayers = world.getEntitiesWithinAABB(EntityPlayer.class, bb, predicate);
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
            ICapabilityMisc misc = nearestPlayer.getCapability(MISC_CAPABILITY, null);
            if (misc == null) {
                return true;
            }
            // Consume fuel
            inventory.withoutSideEffects().extractItem(SLOT_FUEL, 1, false);
            // Extinguish flame and play sound
            this.sideEffects = false;
            world.setBlockState(testPos, Blocks.AIR.getDefaultState());
            this.sideEffects = true;
            WorldUtil.sendFireExtinguishSound(world, testPos);
            // Give effect to player in range
            activate(nearestPlayer, misc, morphTarget, pos, world.provider.getDimension());
            return true;
        }
        return false;
    }
    
    /** Given the neigboring block change, check if the tile
     *  needs to be activated/deactivated. **/
    public void onNeighborChange(BlockPos pos, BlockPos neighborPos) {
        if (this.world.isRemote) {
            return;
        }
        for (EnumFacing facing : EnumFacing.VALUES) {
            BlockPos testPos = pos.offset(facing);
            if (testPos.equals(neighborPos)) {
                continue;
            }
            if (tryDouse(this.world, pos, testPos)) {
                return;
            }
        }
        tryIgnite(this.world, pos, neighborPos);
    }
    
    public void breakBlock(World world, BlockPos pos) {
        deactivate(false);
        final int n = inventory.getSlots();
        for (int i = 0; i < n; ++i) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            if (!InventoryUtil.isEmptyItemStack(itemStack)) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), itemStack);
            }
        }
    }
    
    protected static final String NBT_PLAYER_UUID = "player_uuid";
    protected static final String NBT_LIFETIME_UUID = "lifetime_uuid";
    protected static final String NBT_INVENTORY = "inventory";
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound nbtOut = super.writeToNBT(compound);
        
        nbtOut.setTag(NBT_INVENTORY, inventory.serializeNBT());
        if (playerUUID != null) {
            nbtOut.setString(NBT_PLAYER_UUID, playerUUID.toString());
        }
        if (lifetimeUUID != null) {
            nbtOut.setString(NBT_LIFETIME_UUID, lifetimeUUID.toString());
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
        if (compound.hasKey(NBT_PLAYER_UUID, Serialization.NBT_STRING_ID)) {
            playerUUID = UUID.fromString(compound.getString(NBT_PLAYER_UUID));
        }
        if (compound.hasKey(NBT_LIFETIME_UUID, Serialization.NBT_STRING_ID)) {
            lifetimeUUID = UUID.fromString(compound.getString(NBT_LIFETIME_UUID));
        }

        ICapabilityWorldHumanity.Phylactery oldPhy = new ICapabilityWorldHumanity.Phylactery(lifetimeUUID, lifetimeUUID, getPos(), getWorld().provider.getDimension(), getPhylacteryState(), getMorphTarget());
        checkWorldState(this, oldPhy, lifetimeUUID, playerUUID, true);
    }
}
