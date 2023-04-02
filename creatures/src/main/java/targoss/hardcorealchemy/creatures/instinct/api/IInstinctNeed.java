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

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.INBTSerializable;
import targoss.hardcorealchemy.creatures.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.creatures.instinct.internal.InstinctState;
import targoss.hardcorealchemy.creatures.instinct.network.api.INeedMessenger;

/**
 * Object for keeping track of whether a specific instinct need is being
 * fulfilled or not.
 * 
 * Must implement the default constructor unless a custom InstinctFactory
 * is used.
 * 
 * Will be automatically synced over the network along with other instinct
 * data on player login using NBT serialization.
 */
public interface IInstinctNeed extends INBTSerializable<NBTTagCompound> {
    public static final float DEFAULT_MAX_ALLOWED_INSTINCT = (float)ICapabilityInstinct.MAX_INSTINCT.getDefaultValue() / 2.0F;
    /**
     * Create an instance of this need, with the entity as a
     * parameter to allow for some entity-specific traits. 
     */
    @Nullable IInstinctNeed createInstanceFromMorphEntity(EntityLivingBase morphEntity);
    
    /**
     * Allows the need to stop the effect from being activated after
     * the instinct threshold has been met. This is called for all needs
     * which are part of the same instinct. If at least one need does not
     * want the effect activated, then the effect will not be activated by
     * that instinct.
     * 
     * Note that this is only called when
     * the effect is first activated. The effect will only be deactivated if
     * the instinct value increases above the threshold.
     */
    default boolean shouldActivateEffect(IInstinctState instinctState, InstinctEffect effect) {
        return true;
    }
    
    /**
     * Returns the message that will be displayed in the player's chat on occasion,
     * letting the player know what the need does.
     * This function should have no side-effects if it returns null.
     * 
     * Dry example: "You have the instinctive need to do X"
     * 
     * **SERVER-SIDE ONLY!**
     */
    @Nullable ITextComponent getNeedMessage(InstinctState.NeedStatus needStatus);
    /**
     * Returns the message that will be displayed in the player's chat when
     * this need becomes more unfulfilled. It may not be called if the instinct
     * value has been fluctuating a lot, to prevent chat spam.
     * 
     * Dry example: "You feel the sudden urge to do X"
     * 
     * **SERVER-SIDE ONLY!**
     */
    @Nullable ITextComponent getNeedUnfulfilledMessage(InstinctState.NeedStatus needStatus);
    
    /**
     * Called on player ticks when the instinct
     * is active.
     * 
     * **SERVER-SIDE ONLY!**
     */
    void tick(IInstinctState instinctState);
    
    public default void afterKill(IInstinctState instinctState, EntityLivingBase entity) {
        return;
    }
    
    /**
     * When returning a non-null value, defines a custom INeedMessenger
     * that will be used for syncing this need's data over the network.
     */
    public default INeedMessenger getCustomMessenger() {
        return null;
    }
}
