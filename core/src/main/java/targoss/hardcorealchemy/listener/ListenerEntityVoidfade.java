/*
 * Copyright 2020 asanetargoss
 * 
 * This file is part of the Hardcore Alchemy capstone mod.
 * 
 * The Hardcore Alchemy capstone mod is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3 of the
 * License.
 * 
 * The Hardcore Alchemy capstone mod is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Hardcore Alchemy capstone mod. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.listener;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.items.IItemHandler;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.dimensionhistory.ICapabilityDimensionHistory;
import targoss.hardcorealchemy.capability.entitystate.ICapabilityEntityState;
import targoss.hardcorealchemy.capability.entitystate.ProviderEntityState;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ProviderMisc;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.event.EventPlayerDamageBlockSound;
import targoss.hardcorealchemy.event.EventPlayerInventorySlotSet;
import targoss.hardcorealchemy.item.Items;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.MorphState;

public class ListenerEntityVoidfade extends ConfiguredListener {
    public ListenerEntityVoidfade(Configs configs) {
        super(configs);
    }

    @CapabilityInject(ICapabilityDimensionHistory.class)
    public static final Capability<ICapabilityDimensionHistory> DIMENSION_HISTORY_CAPABILITY = null;

    protected static void applyVoidfade(EntityLivingBase entity, int durationSeconds) {
        if (ModState.isDissolutionLoaded && MorphState.isIncorporeal(entity)) {
            return;
        }
        entity.addPotionEffect(new PotionEffect(Items.POTION_VOIDFADE, durationSeconds * 20, 0, false, true));
    }
    
    public static class DimensionHistoryChangedFlag {
        public static final DimensionHistoryChangedFlag IGNORE_CHANGES = new DimensionHistoryChangedFlag();
        public boolean changed = false;
    }
    
    /**
     * Only Nether Quartz and Dimensional Flux crystals have dimension history.
     * The returned history should always have a length of at least one.
     * WARNING: Be sure to call CapUtil.setVirtualCapability
     */
    public static @Nullable ICapabilityDimensionHistory getOrInitDimensionHistory(ItemStack itemStack, int initialDimension, DimensionHistoryChangedFlag changedFlag) {
        if (itemStack.getItem() != net.minecraft.init.Items.QUARTZ &&
            itemStack.getItem() != Items.DIMENSIONAL_FLUX_CRYSTAL) {
            return null;
        }
        ICapabilityDimensionHistory dimHistoryCap = null;
        if (!CapUtil.hasVirtualCapability(itemStack, DIMENSION_HISTORY_CAPABILITY)) {
            dimHistoryCap = DIMENSION_HISTORY_CAPABILITY.getDefaultInstance();
            changedFlag.changed = true;
        } else {
            dimHistoryCap = CapUtil.getVirtualCapability(itemStack, DIMENSION_HISTORY_CAPABILITY);
        }
        if (dimHistoryCap != null) {
            if (dimHistoryCap.getDimensionHistory().size() == 0) {
                dimHistoryCap.getDimensionHistory().add(initialDimension);
                changedFlag.changed = true;
            }
        }
        return dimHistoryCap;
    }

    public static @Nullable ICapabilityDimensionHistory appendDimensionHistory(ItemStack itemStack, int newDimension, DimensionHistoryChangedFlag changedFlag) {
        ICapabilityDimensionHistory dimHistoryCap = getOrInitDimensionHistory(itemStack, newDimension, changedFlag);
        if (dimHistoryCap != null) {
            List<Integer> dimHistory = dimHistoryCap.getDimensionHistory();
            if (dimHistory.size() < 12) {
                if (newDimension != dimHistory.get(dimHistory.size() - 1)) {
                    dimHistory.add(newDimension);
                    if (dimHistory.size() >= 3 && dimHistory.get(dimHistory.size() - 1) == dimHistory.get(dimHistory.size() - 3)) {
                        dimHistory.remove(dimHistory.size() - 1);
                        dimHistory.remove(dimHistory.size() - 1);
                    }
                    changedFlag.changed = true;
                }
            }
        }
        return dimHistoryCap;
    }
    
    public ITextComponent getDimensionName(Integer dimension) {
        if (dimension != null) {
            DimensionType dimensionType = DimensionManager.getProviderType(dimension);
            if (dimensionType != null) {
                return new TextComponentTranslation("hardcorealchemy.dimension." + dimensionType.getName().toLowerCase().replace(' ', '_').replace('.', '_'));
            }
        }
        return new TextComponentTranslation("hardcorealchemy.dimension.missingno").setStyle(new Style().setObfuscated(true));
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public void onTooltipDimensionalFluxCrystal(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() != Items.DIMENSIONAL_FLUX_CRYSTAL) {
            return;
        }
        ICapabilityDimensionHistory history = getOrInitDimensionHistory(event.getItemStack(), event.getEntityPlayer().world.provider.getDimension(), DimensionHistoryChangedFlag.IGNORE_CHANGES);
        if (history == null) {
            event.getToolTip().add(new TextComponentTranslation("hardcorealchemy.dimensional_flux_crystal.description.origin", getDimensionName(null)).getFormattedText());
            event.getToolTip().add(new TextComponentTranslation("hardcorealchemy.dimensional_flux_crystal.description.fluxed_unknown_one").getFormattedText());
        } else {
            List<Integer> dimensionHistory = history.getDimensionHistory();
            int originDimension = dimensionHistory.get(0);
            ITextComponent originDimensionName = getDimensionName(originDimension);
            event.getToolTip().add(new TextComponentTranslation("hardcorealchemy.dimensional_flux_crystal.description.origin", originDimensionName).getFormattedText());
            if (dimensionHistory.size() <= 2) {
                event.getToolTip().add(new TextComponentTranslation("hardcorealchemy.dimensional_flux_crystal.description.fluxed_unknown_one").getFormattedText());
            } else if (dimensionHistory.size() <= 6) {
                event.getToolTip().add(new TextComponentTranslation("hardcorealchemy.dimensional_flux_crystal.description.fluxed_unknown_multiple").getFormattedText());
            } else {
                event.getToolTip().add(new TextComponentTranslation("hardcorealchemy.dimensional_flux_crystal.description.fluxed_unknown_many").getFormattedText());
            }
        }
    }
    
    @SuppressWarnings("deprecation")
    public static ItemStack fluxifyItem(ItemStack itemStack, int previousDimension, int currentDimension, DimensionHistoryChangedFlag changedFlag, boolean modifyInPlace) {
        ICapabilityDimensionHistory dimHistoryCap = appendDimensionHistory(itemStack, previousDimension, changedFlag);
        if (dimHistoryCap == null) {
            return itemStack;
        }
        dimHistoryCap = appendDimensionHistory(itemStack, currentDimension, changedFlag);
        if (!changedFlag.changed) {
            return itemStack;
        }
        Item newItem = itemStack.getItem();
        List<Integer> dimHistory = dimHistoryCap.getDimensionHistory();
        if (dimHistory.size() == 1) {
            newItem = net.minecraft.init.Items.QUARTZ;
        } else {
            newItem = Items.DIMENSIONAL_FLUX_CRYSTAL;
        }
        ItemStack newHistoryCrystal;
        if (!modifyInPlace) {
            newHistoryCrystal = new ItemStack(newItem, itemStack.stackSize);
        } else {
            newHistoryCrystal = itemStack;
            itemStack.setItem(newItem);
        }
        CapUtil.setVirtualCapability(newHistoryCrystal, DIMENSION_HISTORY_CAPABILITY, dimHistoryCap);
        changedFlag.changed = true;
        return newHistoryCrystal;
    }
    
    protected static class FluxifyItemFunc implements InventoryUtil.ItemFunc {
        public final int previousDimension;
        public final int currentDimension;
        protected final DimensionHistoryChangedFlag changedFlag = new DimensionHistoryChangedFlag(); 

        public FluxifyItemFunc(int previousDimension, int currentDimension) {
            this.previousDimension = previousDimension;
            this.currentDimension = currentDimension;
        }

        @Override
        public boolean apply(IItemHandler inventory, int slot, ItemStack itemStack) {
            changedFlag.changed = false;
            ItemStack newHistoryCrystal = fluxifyItem(itemStack, previousDimension, currentDimension, changedFlag, false);
            if (!changedFlag.changed) {
                return false;
            }
            inventory.extractItem(slot, itemStack.stackSize, false);
            inventory.insertItem(slot, newHistoryCrystal, false);
            return true;
        }
        
    }
    
    protected static void fluxifyItems(EntityLivingBase entity, int previousDimension, int currentDimension) {
        List<IItemHandler> inventories = InventoryUtil.getLocalInventories(entity);
        InventoryUtil.forEachItemRecursive(inventories, new FluxifyItemFunc(previousDimension, currentDimension));
    }
    
    protected static void handleTraveledDimensionally(EntityLivingBase entityLiving, int previousDimension, int currentDimension) {
        applyVoidfade(entityLiving, 20);
        fluxifyItems(entityLiving, previousDimension, currentDimension);
    }
    
    protected void handleEntityLivingEnteringDimension(EntityLivingBase entityLiving, boolean definitelyTraveledToDimension) {
        ICapabilityEntityState state = entityLiving.getCapability(ProviderEntityState.CAPABILITY, null);
        boolean traveledDimensionally = definitelyTraveledToDimension || state.getTraveledDimensionally();
        if (!traveledDimensionally) {
            return;
        }
        if (ModState.isDissolutionLoaded && MorphState.isIncorporeal(entityLiving)) {
            return;
        }
        ICapabilityMisc misc = entityLiving.getCapability(ProviderMisc.MISC_CAPABILITY, null);
        if (misc != null && !misc.getHasChangedDimensionWhileAlive()) {
            misc.setHasChangedDimensionWhileAlive(true);
            return;
        }

        int previousDimension = state.getPreviousDimension();
        int currentDimension = entityLiving.world.provider.getDimension();
        if (previousDimension != currentDimension) {
            handleTraveledDimensionally(entityLiving, previousDimension, currentDimension);
        }
        
        if (state != null) {
            state.setTraveledDimensionally(false);
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        handleEntityLivingEnteringDimension(event.player, true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityVisitDimensionOrRespawn(EntityTravelToDimensionEvent event) {
        Entity entity = event.getEntity();
        ICapabilityEntityState state = entity.getCapability(ProviderEntityState.CAPABILITY, null);
        if (state == null) {
            return;
        }
        state.setTraveledDimensionally(true);
        state.setPreviousDimension(entity.world.provider.getDimension());
    }

    @SubscribeEvent
    public void onEntityEnterDimension(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof EntityLivingBase)) {
            return;
        }
        EntityLivingBase entityLiving = (EntityLivingBase)entity;
        handleEntityLivingEnteringDimension(entityLiving, false);
    }
    
    @SubscribeEvent
    public void onPlayerEnterDimension(PlayerChangedDimensionEvent event) {
        handleEntityLivingEnteringDimension(event.player, event.fromDim != event.toDim);
    }

    @SubscribeEvent
    public void onEntityTouchPortal(LivingUpdateEvent event) {
        EntityLivingBase entityLiving = event.getEntityLiving();
        if (entityLiving.inPortal) {
            applyVoidfade(entityLiving, 5);
        }
    }

    // Prevent entities with voidfade from receiving damage
    @SubscribeEvent(priority=EventPriority.HIGH)
    public void onEntityHurt(LivingHurtEvent event) {
        EntityLivingBase entityLiving = event.getEntityLiving();
        if (entityLiving.isPotionActive(Items.POTION_VOIDFADE)) {
            event.setCanceled(true);
        }
    }
    
    // Prevent players with voidfade from attacking
    @SubscribeEvent(priority=EventPriority.HIGH)
    public void onPlayerAttack(AttackEntityEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        if (event.getEntityPlayer().isPotionActive(Items.POTION_VOIDFADE)) {
            event.setCanceled(true);
            if (player.world.isRemote) {
                player.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 0.25F, 1.0F);
            }
        }
    }

    // Prevent entities with voidfade from attacking
    @SubscribeEvent(priority=EventPriority.HIGH)
    public void onEntityAttack(LivingAttackEvent event) {
        EntityLivingBase entityLiving = event.getEntityLiving();
        if (entityLiving.world.isRemote) {
            return;
        }
        
        DamageSource damageSource = event.getSource();
        if (!(damageSource instanceof EntityDamageSource)) {
            return;
        }
        Entity agressor = ((EntityDamageSource)damageSource).getEntity();
        if (!(agressor instanceof EntityLivingBase)) {
            return;
        }
        
        if (((EntityLivingBase)agressor).isPotionActive(Items.POTION_VOIDFADE)) {
            event.setCanceled(true);
            if (!agressor.world.isRemote) {
                agressor.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 0.7F, 1.0F);
            }
        }
    }

    // Prevent players with voidfade from mining
    @SubscribeEvent(priority=EventPriority.HIGH)
    public void onPlayerDig(PlayerEvent.BreakSpeed event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null) {
            return;
        }
        if (player.isPotionActive(Items.POTION_VOIDFADE)) {
            event.setCanceled(true);
        }
    }

    // Prevent players with voidfade from right clicking blocks or placing blocks (using items is fine)
    @SubscribeEvent(priority=EventPriority.HIGH)
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player == null) {
            return;
        }
        if (!player.isPotionActive(Items.POTION_VOIDFADE)) {
            return;
        }
        boolean stoppedUse = false;
        BlockPos pos = event.getPos();
        World world = event.getWorld();
        if (pos != null && world != null && !world.isAirBlock(pos)) {
            event.setUseBlock(Result.DENY);
            stoppedUse = true;
        }
        ItemStack itemStack = event.getItemStack();
        if (!InventoryUtil.isEmptyItemStack(itemStack)) {
            Item item = itemStack.getItem();
            if (item instanceof ItemBlock) {
                event.setUseItem(Result.DENY);
                stoppedUse = true;
            }
        }
        if (stoppedUse) {
            player.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 0.2F, 1.0F);
        }
    }
    
    protected static class SoundEventMasqueradingAsBlock extends net.minecraft.block.SoundType {
        public SoundEventMasqueradingAsBlock(float volume, float pitch, SoundEvent sound) {
            super(volume, pitch, sound, sound, sound, sound, sound);
        }
    }
    
    @SubscribeEvent
    public void onPlayerDamageBlockSound(EventPlayerDamageBlockSound event) {
        if (event.player.isPotionActive(Items.POTION_VOIDFADE)) {
            event.soundType = new SoundEventMasqueradingAsBlock(0.7F, 2.0F, SoundEvents.ENTITY_ENDERMEN_TELEPORT);
        }
    }
    
    @SubscribeEvent
    public void onPlayerInventorySlotSet(EventPlayerInventorySlotSet event) {
        ItemStack itemStack = event.itemStack;
        if (itemStack == null) {
            return;
        }
        int dimension = event.inventoryPlayer.player.world.provider.getDimension();
        DimensionHistoryChangedFlag changedFlag = new DimensionHistoryChangedFlag();
        // modifyInPlace must be true or item pickup won't work
        itemStack = fluxifyItem(itemStack, dimension, dimension, changedFlag, true);
        if (!changedFlag.changed) {
            return;
        }
        event.itemStack = itemStack;
    }
}
