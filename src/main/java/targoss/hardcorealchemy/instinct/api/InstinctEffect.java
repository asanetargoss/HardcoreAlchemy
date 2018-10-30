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

package targoss.hardcorealchemy.instinct.api;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

/**
 * Stateless class for defining an effect which changes
 * player capabilities/behavior.
 */
public abstract class InstinctEffect extends IForgeRegistryEntry.Impl<InstinctEffect> {
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
