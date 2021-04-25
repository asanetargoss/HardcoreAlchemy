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
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.entitystate.ICapabilityEntityState;
import targoss.hardcorealchemy.capability.entitystate.ProviderEntityState;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ProviderMisc;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.event.EventPlayerDamageBlockSound;
import targoss.hardcorealchemy.item.Items;
import targoss.hardcorealchemy.util.InventoryUtil;
import targoss.hardcorealchemy.util.MorphState;

public class ListenerEntityVoidfade extends ConfiguredListener {
    public ListenerEntityVoidfade(Configs configs) {
        super(configs);
    }

    protected static void applyVoidfade(EntityLivingBase entity, int durationSeconds) {
        if (ModState.isDissolutionLoaded && MorphState.isIncorporeal(entity)) {
            return;
        }
        entity.addPotionEffect(new PotionEffect(Items.POTION_VOIDFADE, durationSeconds * 20, 0, false, true));
    }
    
    protected void handleEntityLeavingDimension(Entity entity) {
        ICapabilityEntityState state = entity.getCapability(ProviderEntityState.CAPABILITY, null);
        if (state == null) {
            return;
        }
        state.setTraveledDimensionally(true);
    }
    
    protected void handleEntityLivingEnteringDimension(EntityLivingBase entityLiving, boolean definitelyTraveledToDimension) {
        ICapabilityEntityState state = entityLiving.getCapability(ProviderEntityState.CAPABILITY, null);
        boolean traveledDimensionally = definitelyTraveledToDimension;
        if (state != null) {
            traveledDimensionally |= state.getTraveledDimensionally();
        }
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
        applyVoidfade(entityLiving, 20);
        if (state != null) {
            state.setTraveledDimensionally(false);
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerLoggedInEvent event) {
        handleEntityLivingEnteringDimension(event.player, true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        handleEntityLeavingDimension(event.getEntity());
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
}
