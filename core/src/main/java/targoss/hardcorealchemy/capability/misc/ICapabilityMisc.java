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

package targoss.hardcorealchemy.capability.misc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;

/**
 * For information too small to warrant a separate capability
 * Currently does not sync over the network or persist on death, since that's not needed at the moment.
 */
public interface ICapabilityMisc {
    /** Get the player's UUID for their current life. */
    UUID getLifetimeUUID();
    void setLifetimeUUID(UUID uuid);
    int getLastIncantationTick();
    void setLastIncantationTick(int lastIncantationTick);
    boolean getHasChangedDimensionWhileAlive();
    void setHasChangedDimensionWhileAlive(boolean hasChangedDimensionWhileAlive);
    String getLastLoginVersion();
    void setLastLoginVersion(String lastLoginVersion);
    List<ItemStack> getPendingInventoryGifts();
    void setPendingInventoryGifts(List<ItemStack> pendingInventoryGifts);
    
    // These are not stored
    Map<Potion, PotionEffect> getActivePotionsCache();
    void setActivePotionsCache(Map<Potion, PotionEffect> activePotionsCache);
    int getFireCache();
    void setFireCache(int fireCache);
    /** Not for general use - May be null even if this entity has been attacked recently */
    @Nullable DamageSource getLastDamageSource();
    void setLastDamageSource(DamageSource lastDamageSource);
    /** May be -1 if unset */
    int getLastItem();
    void setLastItem(int lastItem);
    /** Sent from client, used for storing render state when crafting timefrozen items */
    Map<String, Float> getEnqueuedItemModelProperties();
    void setEnqueuedItemModelProperties(Map<String, Float> enqueuedItemModelProperties);
}
