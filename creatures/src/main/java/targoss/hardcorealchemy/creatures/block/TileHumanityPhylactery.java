/*
 * Copyright 2017-2023 asanetargoss
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
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
import targoss.hardcorealchemy.item.ConditionalItemHandler;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.Serialization;
import targoss.hardcorealchemy.util.WorldUtil;

public class TileHumanityPhylactery extends TileEntity {
    
    @CapabilityInject(IItemHandler.class)
    public static final Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = null;
    protected static final int SLOT_MORPH_TARGET = 0;
    protected static final int SLOT_TRUE_FORM    = 1;
    protected static final int SLOT_FUEL         = 2;
    protected static final int SLOT_COUNT        = 3;
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
    
    public TileHumanityPhylactery() {} // Called by Forge via reflection
    
    protected static class Slot extends SlotItemHandler {

        public Slot(ItemStackHandler handler, int index, int xPosition, int yPosition) {
            super(handler, index, xPosition, yPosition);
        }
        
        @Override
        public boolean isItemValid(@Nullable ItemStack stack) {
            switch (getSlotIndex()) {
            case SLOT_MORPH_TARGET:
                if (getMorphTarget(stack) == null) {
                    return false;
                }
                break;
            case SLOT_TRUE_FORM:
                if (!isTrueFormSeal(stack)) {
                    return false;
                }
                break;
            case SLOT_FUEL:
                if (!isSufficientFuel(stack)) {
                    return false;
                }
                break;
            default:
                return false;
            }
            
            return super.isItemValid(stack);
        }
    }
    
    protected class Inventory extends ConditionalItemHandler {
        protected boolean worldLoad = false;
        protected boolean sideEffects = true;
        public boolean changed = false;
        
        public Inventory() {
            super(SLOT_COUNT);
        }

        @Override
        protected int getStackLimit(int slot, ItemStack stack)
        {
            return slot == SLOT_FUEL ? 64 : 1;
        }

        @Override
        public SlotItemHandler createSlot(IInventory inventory, int index, int xPosition, int yPosition) {
            return new Slot(this, index, xPosition, yPosition);
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            
            if (worldLoad) {
                worldLoad = false;
                return;
            }
            
            // If an inactive seal of true form is placed in the slot, make the seal active again
            if (sideEffects && slot == SLOT_TRUE_FORM) {
                ItemStack stack = this.stacks[slot];
                if (!InventoryUtil.isEmptyItemStack(stack) && ItemSealOfForm.hasInactiveTag(stack)) {
                    ItemStack newStack = stack.copy();
                    ItemSealOfForm.setInactiveTag(newStack, false);
                    this.stacks[slot] = newStack;
                    changed = true;
                }
            }
            if (!sideEffects) {
                changed = true;
            }

            // Make sure new inventory contents are saved
            markDirty();
            if (changed) {
                // Sync altered inventory state to client
                markNeedsUpdate();
            }
            
            if (!sideEffects) {
                sideEffects = true;
                return;
            }
            checkShouldStillBeActive();
        }
        
        protected Inventory withWorldLoad() {
            this.worldLoad = true;
            return this;
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
            if (humanity.getIsHumanFormInPhylactery()) {
                return false;
            }
            return !humanity.getHasForgottenHumanForm();
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
            if (humanity.getIsHumanFormInPhylactery()) {
                return false;
            }
            return humanity.getHasForgottenHumanForm();
        }
    }

    // Dirtied in onContentsChanged
    public final Inventory inventory = new Inventory();
    
    // Dirtied in setters
    protected UUID permanentUUID = null;
    protected UUID lifetimeUUID = null;
    protected boolean dormant = false;
    
    // Rotation rate of the outer ring, in revolutions per second
    // Used for syncing and rendering
    public static final float ROTATION_FREQUENCY = 0.0083F;
    
    protected static long getTimeModuloRotation(long time) {
        long modulo = (long)(1000 * ROTATION_FREQUENCY);
        long modtime = time % modulo;
        if (modtime < 0) {
            modtime += modulo;
        }
        return modtime;
    }
    
    public float getRotationOffsetFromTime(long time) {
        float modulo = 2.0F * (float)Math.PI;
        float modRot = (ROTATION_FREQUENCY * (float)time / 1000.0F) % modulo;
        if (modRot < 0.0F) {
            modRot += modulo;
        }
        return modRot;
    }

    // Dirtied in setters, but prefer client prediction when practical
    // activeFramePhaseTime is milliseconds since the Unix epoch, modulo the phylactery rotation rate. It determines the rotation of the phylactery when it is active
    // inactiveFramePhase determines the rotation of the phylactery when it is inactive
    // Angle and phase are in radians and determine the orientation of the frames
    public long activeFramePhaseTime = 0;
    public float initialFrameAngle = 0;
    public float inactiveFramePhase = 0;
    
    // Used for rendering
    public double tickTime = 0;
    @SideOnly(Side.CLIENT)
    public double particleTime = 0;
    
    protected void recordPhaseTimeStart(boolean wasVisiblyActive) {
        serializePhase(wasVisiblyActive);
        activeFramePhaseTime = getTimeModuloRotation(-System.currentTimeMillis());
    }
    
    protected void serializePhase(boolean wasVisiblyActive) {
        long activeTime = wasVisiblyActive ? System.currentTimeMillis() : 0L;
        inactiveFramePhase += getRotationOffsetFromTime(activeTime - activeFramePhaseTime);
        inactiveFramePhase = inactiveFramePhase % (2.0F * (float)Math.PI);
        activeFramePhaseTime = 0;
        tickTime = 0;
    }
    
    protected void setActive(@Nonnull UUID permanentUUID, @Nonnull UUID lifetimeUUID, boolean isWorldLoad) {
        assert(permanentUUID != null);
        assert(lifetimeUUID != null);
        
        boolean wasVisiblyActive = isVisiblyActive();
        
        boolean dirty = false;
        if (this.permanentUUID != permanentUUID) {
            dirty = true;
            this.permanentUUID = permanentUUID;
        }
        if (this.lifetimeUUID != lifetimeUUID) {
            dirty = true;
            this.lifetimeUUID = lifetimeUUID;
        }
        
        if (isActive() && hasTrueFormSeal()) {
            setTrueFormSealDormant(false, isWorldLoad);
        }
        if (dirty && !isWorldLoad) {
            recordPhaseTimeStart(wasVisiblyActive);
            
            if (!this.world.isRemote) {
                markDirty();
                markNeedsUpdate();
            }
        }
    }
    
    /**
     * 
     * @param isWorldLoad - If the world is loaded
     */
    protected void setDeactivated(boolean isWorldLoad) {
        boolean wasVisiblyActive = isVisiblyActive();
        
        boolean dirty = false;
        if (this.permanentUUID != null) {
            this.permanentUUID = null;
            dirty = true;
        }
        if (this.lifetimeUUID != null) {
            this.lifetimeUUID = null;
            dirty = true;
        }
        if (this.dormant) {
            this.dormant = false;
            dirty = true;
        }
        
        if (dirty && !isWorldLoad) {
            serializePhase(wasVisiblyActive);
            
            if (!this.world.isRemote) {
                markDirty();
                markNeedsUpdate();
            }
        }
    }

    protected void setDormant(boolean dormant, boolean isWorldLoad) {
        boolean wasVisiblyActive = isVisiblyActive();
        
        boolean dirty = false;
        if (this.dormant != dormant) {
            this.dormant = dormant;
            dirty = true;
        }
        if (dormant && hasTrueFormSeal()) {
            setTrueFormSealDormant(true, isWorldLoad);
        }
        
        if (dirty && !isWorldLoad) {
            serializePhase(wasVisiblyActive);
            
            if (!this.world.isRemote) {
                markDirty();
                markNeedsUpdate();
            }
        }
    }
    
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
                phyTE.setDormant(worldStateDormant, isWorldLoad);
            }
        }
    }
    
    public boolean isActive() {
        return permanentUUID != null;
    }
    
    public boolean isDormant() {
        return dormant;
    }
    
    public boolean isVisiblyActive() {
        return isActive() && !isDormant();
    }
    
    protected static void create(@Nullable TileHumanityPhylactery phyTE, @Nullable EntityPlayer player, @Nullable ICapabilityMisc misc, AbstractMorph morphTarget, @Nullable ICapabilityWorldHumanity.Phylactery oldPhy, ICapabilityWorldHumanity.Phylactery newPhy, boolean isWorldLoad) {
        if (phyTE != null) {
            phyTE.setActive(newPhy.permanentUUID, newPhy.lifetimeUUID, isWorldLoad);
            
            if (!isWorldLoad) {
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
                boolean ownerChanged = !Objects.equals(newPhy.permanentUUID, oldPhy.permanentUUID) || !Objects.equals(oldPhy.lifetimeUUID, newPhy.lifetimeUUID);
                boolean stateChanged = newPhy.state != oldPhy.state || !Objects.equals(newPhy.morphTarget, oldPhy.morphTarget);
                if (ownerChanged) {
                    MinecraftForge.EVENT_BUS.post(new EventHumanityPhylactery.Destroy(oldPhy.lifetimeUUID, oldPhy.permanentUUID, oldPhy.pos, oldPhy.dimension));
                }
                if (ownerChanged || stateChanged) {
                    MinecraftForge.EVENT_BUS.post(new EventHumanityPhylactery.Recreate(newPhy.permanentUUID, newPhy.lifetimeUUID, newPhy.morphTarget, newPhy.pos, newPhy.dimension));
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
        
        ICapabilityWorldHumanity.Phylactery oldPhy = new ICapabilityWorldHumanity.Phylactery(this.lifetimeUUID, this.permanentUUID, getPos(), getWorld().provider.getDimension(), ICapabilityWorldHumanity.State.DEACTIVATED, null);
        ICapabilityWorldHumanity.Phylactery newPhy = new ICapabilityWorldHumanity.Phylactery(misc.getLifetimeUUID(), misc.getPermanentUUID(), getPos(), getWorld().provider.getDimension(), ICapabilityWorldHumanity.State.ACTIVE, morphTarget);
        worldHumanity.registerPhylactery(newPhy);
        create(this, player, misc, morphTarget, oldPhy, newPhy, false);
    }
    
    protected static void deactivate(@Nullable TileHumanityPhylactery phyTE, ICapabilityWorldHumanity.Phylactery oldPhy, boolean isWorldLoad) {
        assert(oldPhy != null);
        if (!isWorldLoad && oldPhy.state != State.DEACTIVATED && oldPhy.state != State.DORMANT) {
            ICapabilityWorldHumanity worldHumanity = UniverseCapabilityManager.INSTANCE.getCapability(HUMANITY_WORLD_CAPABILITY);
            if (worldHumanity != null) {
                boolean wasRegistered = worldHumanity.unregisterPhylactery(oldPhy.lifetimeUUID, oldPhy.permanentUUID, oldPhy.pos, oldPhy.dimension);
                if (wasRegistered) {
                    MinecraftForge.EVENT_BUS.post(new EventHumanityPhylactery.Destroy(oldPhy.lifetimeUUID, oldPhy.permanentUUID, oldPhy.pos, oldPhy.dimension));
                }
            }
            
        }
        if (phyTE != null) {
            phyTE.setDeactivated(isWorldLoad);
        }
    }
    
    public void deactivate(boolean isWorldLoad) {
        ICapabilityWorldHumanity.Phylactery oldPhy = new ICapabilityWorldHumanity.Phylactery(lifetimeUUID, permanentUUID, this.getPos(), getWorld().provider.getDimension(), this.getPhylacteryState(), this.getMorphTarget());
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
    
    protected void setTrueFormSealDormant(boolean dormant, boolean isWorldLoad) {
        ItemStack itemStack = inventory.getStackInSlot(SLOT_TRUE_FORM);
        if (InventoryUtil.isEmptyItemStack(itemStack)) {
            return;
        }
        if (!isTrueFormSeal(itemStack)) {
            assert(false);
            return;
        }
        if (dormant == ItemSealOfForm.hasInactiveTag(itemStack)) {
            return;
        }
        ItemStack newStack = itemStack.copy();
        ItemSealOfForm.setInactiveTag(newStack, dormant);
        if (isWorldLoad) {
            inventory.withWorldLoad().setStackInSlot(SLOT_TRUE_FORM, newStack);
        } else {
            inventory.withoutSideEffects().setStackInSlot(SLOT_TRUE_FORM, newStack);
        }
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
        boolean needTrueFormSeal = !hasTrueFormSeal();
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
            IMorphing morphing = Morphing.get(nearestPlayer);
            if (morphing == null) {
                return true;
            }
            
            // Consume resources before activation (these do not affect the player)
            
            // Consume fuel
            inventory.withoutSideEffects().extractItem(SLOT_FUEL, 1, false);
            // Grand the player the morph
            boolean needsMorphTargetSeal = !morphing.acquiredMorph(morphTarget);
            if (needsMorphTargetSeal) {
                // Destroy the morph target seal
                inventory.setStackInSlot(SLOT_MORPH_TARGET, InventoryUtil.ITEM_STACK_EMPTY);
            }
            if (needTrueFormSeal) {
                // Create the human form seal
                ItemStack trueFormSeal = new ItemStack(SEAL_OF_FORM);
                ItemSealOfForm.setHumanTag(trueFormSeal);
                inventory.setStackInSlot(SLOT_TRUE_FORM, trueFormSeal);
            }
            // Extinguish flame and play sound
            this.sideEffects = false;
            world.setBlockState(testPos, Blocks.AIR.getDefaultState());
            this.sideEffects = true;
            WorldUtil.sendFireExtinguishSound(world, testPos);
            
            // Actually activate the phylactery (player affected via event listener)
            
            activate(nearestPlayer, misc, morphTarget, pos, world.provider.getDimension());
            
            return true;
        }
        return false;
    }
    
    protected void checkShouldStillBeActive() {
        if (getWorld().isRemote) {
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

    public void neighborChanged() {
        checkShouldStillBeActive();
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
    protected static final String NBT_ACTIVE_FRAME_PHASE_TIME = "phase_time";
    protected static final String NBT_INITIAL_FRAME_ANGLE = "angle";
    protected static final String NBT_INACTIVE_FRAME_PHASE = "phase";
    protected static final String UPDATE_NBT_ACTIVE = "active";
    protected static final String UPDATE_NBT_DORMANT = "dormant";
    protected static final String UPDATE_NBT_INVENTORY = NBT_INVENTORY;
    protected static final String UPDATE_NBT_ACTIVE_FRAME_PHASE_TIME = NBT_ACTIVE_FRAME_PHASE_TIME;
    protected static final String UPDATE_NBT_INITIAL_FRAME_ANGLE = NBT_INITIAL_FRAME_ANGLE;
    protected static final String UPDATE_NBT_INACTIVE_FRAME_PHASE = NBT_INACTIVE_FRAME_PHASE;
    protected static final UUID ANONYMOUS_ID = new UUID(1, 1);
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound nbtOut = super.writeToNBT(compound);
        
        nbtOut.setTag(NBT_INVENTORY, inventory.serializeNBT());
        if (permanentUUID != null) {
            nbtOut.setString(NBT_PLAYER_UUID, permanentUUID.toString());
        }
        if (lifetimeUUID != null) {
            nbtOut.setString(NBT_LIFETIME_UUID, lifetimeUUID.toString());
        }
        serializePhase(isVisiblyActive());
        nbtOut.setLong(NBT_ACTIVE_FRAME_PHASE_TIME, activeFramePhaseTime);
        nbtOut.setFloat(NBT_INITIAL_FRAME_ANGLE, initialFrameAngle);
        nbtOut.setFloat(NBT_INACTIVE_FRAME_PHASE, inactiveFramePhase);
        
        return nbtOut;
    }
    
    @Override
    public void setWorldCreate(World world) {
        this.world = world;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        
        assert(this.world != null);
        
        if (compound.hasKey(NBT_INVENTORY, Serialization.NBT_COMPOUND_ID)) {
            NBTTagCompound inventoryNBT = compound.getCompoundTag(NBT_INVENTORY);
            inventory.deserializeNBT(inventoryNBT);
        }
        if (compound.hasKey(NBT_PLAYER_UUID, Serialization.NBT_STRING_ID)) {
            permanentUUID = UUID.fromString(compound.getString(NBT_PLAYER_UUID));
        }
        if (compound.hasKey(NBT_LIFETIME_UUID, Serialization.NBT_STRING_ID)) {
            lifetimeUUID = UUID.fromString(compound.getString(NBT_LIFETIME_UUID));
        }
        activeFramePhaseTime = compound.getLong(NBT_ACTIVE_FRAME_PHASE_TIME);
        initialFrameAngle = compound.getFloat(NBT_INITIAL_FRAME_ANGLE);
        inactiveFramePhase = compound.getFloat(NBT_INACTIVE_FRAME_PHASE);
    }
    
    @Override
    public void onLoad() {
        super.onLoad();
        
        ICapabilityWorldHumanity.Phylactery oldPhy = new ICapabilityWorldHumanity.Phylactery(lifetimeUUID, permanentUUID, getPos(), getWorld().provider.getDimension(), getPhylacteryState(), getMorphTarget());
        checkWorldState(this, oldPhy, lifetimeUUID, permanentUUID, true);
    }
    
    protected boolean sendInventory = false;
    
    /** Simple chunk invalidator which ignores block state. Contrast with World.setBlockState. Not suitable for real-time updates. */
    protected void markNeedsUpdate() {
        getWorld().checkLight(getPos());
        IBlockState currentState = world.getBlockState(getPos());
        getWorld().markAndNotifyBlock(pos, getWorld().getChunkFromBlockCoords(getPos()), currentState, currentState, 1 | 2 /* block update and send change to clients */);
    }
    
    /** Client-side counterpart to markNeedsUpdate */
    protected void clientGotUpdate() {
        getWorld().checkLight(getPos());
    }
    
    protected static final int NOT_A_VANILLA_TILE_ENTITY = 99999;

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(getPos(), NOT_A_VANILLA_TILE_ENTITY, getUpdateTag());
    }
    
    @Override
    public void onDataPacket(net.minecraft.network.NetworkManager net, net.minecraft.network.play.server.SPacketUpdateTileEntity pkt)
    {
        handleUpdateTag(pkt.getNbtCompound());
    }

    protected NBTTagCompound inventoryTag = null;
    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound updateTag = super.getUpdateTag();
        
        boolean active = isActive();
        if (active) {
            updateTag.setBoolean(UPDATE_NBT_ACTIVE, active);
        }
        boolean dormant = isDormant();
        if (dormant) {
            updateTag.setBoolean(UPDATE_NBT_DORMANT, dormant);
        }
        if (inventory.changed) {
            inventory.changed = false;
            inventoryTag = inventory.serializeNBT();
        }
        updateTag.setTag(UPDATE_NBT_INVENTORY, inventoryTag);
        updateTag.setLong(UPDATE_NBT_ACTIVE_FRAME_PHASE_TIME, activeFramePhaseTime);
        updateTag.setFloat(UPDATE_NBT_INITIAL_FRAME_ANGLE, initialFrameAngle);
        updateTag.setFloat(UPDATE_NBT_INACTIVE_FRAME_PHASE, inactiveFramePhase);
        
        return updateTag;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag) {
        super.handleUpdateTag(tag);
        
        boolean wasVisiblyActive = isVisiblyActive();
        
        boolean active = false;
        if (tag.hasKey(UPDATE_NBT_ACTIVE, Serialization.NBT_BOOLEAN_ID)) {
            active = tag.getBoolean(UPDATE_NBT_ACTIVE);
        }
        if (active) {
            this.permanentUUID = ANONYMOUS_ID;
            this.lifetimeUUID = ANONYMOUS_ID;
        }
        else {
            this.permanentUUID = null;
            this.lifetimeUUID = null;
        }
        
        boolean dormant = false;
        if (tag.hasKey(UPDATE_NBT_DORMANT, Serialization.NBT_BOOLEAN_ID)) {
            dormant = tag.getBoolean(UPDATE_NBT_DORMANT);
        }
        this.dormant = dormant;
        if (tag.hasKey(UPDATE_NBT_INVENTORY, Serialization.NBT_COMPOUND_ID)) {
            NBTTagCompound inventoryTag = tag.getCompoundTag(UPDATE_NBT_INVENTORY);
            if (this.inventoryTag == null || !this.inventoryTag.equals(inventoryTag)) {
                this.inventoryTag = inventoryTag;
                inventory.deserializeNBT(inventoryTag);
            }
        }
        
        float oldInitialFrameAngle = this.initialFrameAngle;
        this.initialFrameAngle = tag.getFloat(UPDATE_NBT_INITIAL_FRAME_ANGLE);
        boolean isVisiblyActive = isVisiblyActive();
        if ((oldInitialFrameAngle != this.initialFrameAngle) || (activeFramePhaseTime == 0L && inactiveFramePhase == 0.0F)) {
            activeFramePhaseTime = tag.getLong(UPDATE_NBT_ACTIVE_FRAME_PHASE_TIME);
            if (isActive()) {
                tickTime = (System.currentTimeMillis() - activeFramePhaseTime) * 20.0 / 1000.0;
            }
            else {
                tickTime = 0;
            }
            inactiveFramePhase = tag.getFloat(UPDATE_NBT_INACTIVE_FRAME_PHASE);
        }
        else if (wasVisiblyActive != isVisiblyActive) {
            // Prefer client-side prediction
            if (isVisiblyActive) {
                recordPhaseTimeStart(wasVisiblyActive);
            }
            else {
                serializePhase(wasVisiblyActive);
            }
        }

        clientGotUpdate();
    }
}
