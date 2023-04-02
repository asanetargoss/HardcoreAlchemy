/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.capability.itemcontainer;

import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * NOT an inventory. An item is stored inside, but
 * you aren't supposed to extract it like you would
 * with a chest/backpack/etc.
 */
public interface ICapabilityItemContainer {
    ItemStack getContainedItem();
    void setContainedItem(ItemStack containedItem);
    // TODO: Rename propertyOverrides to something else more sensible
    // TODO: ResourceLocation, not String
    /** For model rendering (initialized on the client side, but stored on the server-side)
     * There are strict limits on its size due to requiring data sent from the client.
     * */
    Map<ResourceLocation, Float> getPropertyOverrides();
    void setPropertyOverrides(Map<ResourceLocation, Float> propertyOverrides);
}
