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

package targoss.hardcorealchemy.instinct;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import targoss.hardcorealchemy.capability.instincts.ICapabilityInstinct;

/**
 * Object for altering player state for permanent morphs.
 * Must define a default constructor, unless you plan on
 * creating your own InstinctFactory.
 */
public interface IInstinct extends INBTSerializable<NBTTagCompound> {
    public static final float DEFAULT_MAX_ALLOWED_INSTINCT = (float)ICapabilityInstinct.MAX_INSTINCT.getDefaultValue() / 2.0F;
    
    /**
     * Whether this instinct class should apply to players
     * permanently morphed as this entity.
     */
    boolean doesMorphEntityHaveInstinct(EntityLivingBase morphEntity);
    /**
     * Create an instance of this instinct class, with the entity as a
     * parameter to allow for some entity-specific traits. 
     */
    IInstinct createInstanceFromMorphEntity(EntityLivingBase morphEntity);
    /**
     * Get the selection weight of this instinct class
     */
    default float getWeight(EntityLivingBase morphEntity) {
        return 1.0F;
    }
    
    /**
     * Returns the message that will be displayed in the player's chat on occasion,
     * letting the player know what the instinct does.
     * 
     * Dry example: "You have the instinctive need to do X"
     */
    @Nullable ITextComponent getNeedMessage(EntityPlayer player);
    /**
     * Returns the message that will be displayed in the player's chat when the
     * instinct has just been activated.
     * 
     * Dry example: "You feel the sudden urge to do X"
     */
    ITextComponent getNeedMessageOnActivate(EntityPlayer player);
    /**
     * The max instinct value for which this instinct can be
     * activated.
     */
    public default float getMaxAllowedInstinct() {
        return DEFAULT_MAX_ALLOWED_INSTINCT;
    }
    
    /** Whether tick() should continue to be called.
     * Checked every player tick for which
     * getMaxAllowedInstinct() <= player's current instinct,
     * and this instinct has been selected to be active.
     */
    boolean shouldStayActive(EntityPlayer player);
    /**
     * What to do when the instinct becomes
     * active, just before tick() is called.
     */
    void onActivate(EntityPlayer player);
    /**
     * What to do when the instinct becomes
     * inactive or is removed.
     */
    void onDeactivate(EntityPlayer player);
    /**
     * Called on player ticks when the instinct
     * is active.
     */
    void tick(EntityPlayer player);
    
    /**
     * Whether the player can use (right click) the block.
     *  Only called if this instinct is active.
     */
    public default boolean canInteract(EntityPlayer player, BlockPos pos, Block block) {
        return true;
    }
    /**
     * Whether the player can use (right click OR left click) the item.
     *  Only called if this instinct is active.
     *  Due to how digging works, repeatedly showing a dialogue message is
     *  not recommended
     */
    public default boolean canInteract(EntityPlayer player, ItemStack itemStack) {
        return true;
    }
    /** 
     * Whether the player can attack the entity.
     *  Only called if this instinct is active.
     *  Server-side only in most cases, so do not rely on this running on the client.
     */
    public default boolean canAttack(EntityPlayer player, EntityLivingBase entity) {
        return true;
    }
    /**
     * Called after a player attacks an entity.
     *  Only called if this instinct is active.
     *  **Server-side only!**
     */
    public default void afterKill(EntityPlayer player, EntityLivingBase entity) {
        return;
    }
    /**
     * Called after a player breaks a block,
     * with the opportunity to change the harvest drops.
     *  Only called if this instinct is active.
     */
    public default void afterBlockHarvest(EntityPlayer player, HarvestDropsEvent event) {
        return;
    }
    /**
     * Called after a player strikes the killing blow on an entity,
     * with the opportunity to change the loot drops.
     *  Only called if this instinct is active.
     *  **Server-side only!**
     */
    public default void afterKillEntityForDrops(EntityPlayer player, LivingDropsEvent event) {
        return;
    }
    
    /**
     * Called each player tick when no instinct
     * is active. If at least one instinct returns a positive
     * value, the default per-tick instinct decrease is disabled.
     */
    public default float getInactiveChangeOnTick(EntityPlayer player) {
        return 0.0F;
    }
    /**
     * Called after each player kill when no instinct
     * is active.
     *  **Server-side only!** (but the instinct value will be synced for you)
     */
    public default float getInactiveChangeOnKill(EntityPlayer player, EntityLivingBase entity) {
        return 0.0F;
    }
}
