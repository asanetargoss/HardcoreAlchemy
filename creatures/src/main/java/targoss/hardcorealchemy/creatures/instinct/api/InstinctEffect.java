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

package targoss.hardcorealchemy.creatures.instinct.api;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

/**
 * Single-instance object for defining an effect which changes
 * player capabilities/behavior.
 * 
 * The magnitude of the amplifier determines the strength of the effect.
 * The following interpretation of the amplifier is recommended:
 * Below 0.0: Undefined behavior.
 * Between 0.0 and 1.0 inclusive: Player experiences warnings, flavor effects, and
 *   effects which affect gameplay but not enough to require the player to change their behavior.
 * Above 1.0: Player experiences aspects of effect which may significantly affect gameplay,
 *   increasing necessity to fulfill their instincts.
 * At or above 2.0: All aspects of the InstinctEffect are active, although may become even more severe at
 *   higher amplifier values.
 * 
 * When using effects not affected by the amplifier, an amplifier value of 1.0 is recommended.
 */
public abstract class InstinctEffect extends IForgeRegistryEntry.Impl<InstinctEffect> {
    /**
     * If non-null, this constructs the data container associated with the effect when needed.
     * All state goes in here. It is up to the effect creator to decide how to
     * manage the state. The data will persist until instincts are cleared.
     */
    public IInstinctEffectData createData() {
        return null;
    }
    
    /**
     * What to do when the effect becomes
     * active, just before tick() is called.
     */
    public abstract void onActivate(EntityPlayer player, float amplifier);
    /**
     * What to do when the effect becomes
     * inactive or is removed.
     */
    public abstract void onDeactivate(EntityPlayer player, float amplifier);
    /**
     * Called on player ticks when the effect
     * is active.
     */
    public abstract void tick(EntityPlayer player, float amplifier);
    
    /**
     * Whether the player can use (right click) the block.
     *  Only called if this effect is active.
     */
    public boolean canInteract(EntityPlayer player, float amplifier, BlockPos pos, Block block) {
        return true;
    }
    /**
     * Whether the player can use (right click OR left click) the item.
     *  Only called if this effect is active.
     *  Due to how digging works, repeatedly showing a dialogue message is
     *  not recommended
     */
    public boolean canInteract(EntityPlayer player, float amplifier, ItemStack itemStack) {
        return true;
    }
    
    /**
     * Called when a player tries to place a block
     */
    public boolean canPlaceBlock(EntityPlayer player, float amplifier, BlockPos pos, Block block) {
        return true;
    }
    
    /** 
     * Whether the player can attack the entity.
     *  Only called if this effect is active.
     *  Server-side only in most cases, so do not rely on this running on the client.
     */
    public boolean canAttack(EntityPlayer player, float amplifier, EntityLivingBase entity) {
        return true;
    }
    
    /**
     * Called after a player strikes the killing blow on an entity,
     * with the opportunity to change the loot drops.
     *  Only called if this effect is active.
     *  **Server-side only!**
     */
    public void onEntityDrops(EntityPlayer player, float amplifier, LivingDropsEvent event) {
        return;
    }
    
    /**
     * Called after a player breaks a block,
     * with the opportunity to change the harvest drops.
     *  Only called if this effect is active.
     */
    public void onBlockDrops(EntityPlayer player, float amplifier, HarvestDropsEvent event) {
        return;
    }
}
