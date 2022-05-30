/*
 * Copyright 2017-2022 asanetargoss
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

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.util.InventoryUtil;

public class CapabilityItemContainer implements ICapabilityItemContainer {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "item_container");
    
    ItemStack containedItem = InventoryUtil.ITEM_STACK_EMPTY;
    Map<ResourceLocation, Float> propertyOverrides = new HashMap<>();

    @Override
    public ItemStack getContainedItem() {
        return containedItem;
    }

    @Override
    public void setContainedItem(ItemStack containedItem) {
        this.containedItem = containedItem;
    }

    @Override
    public Map<ResourceLocation, Float> getPropertyOverrides() {
        return propertyOverrides;
    }

    @Override
    public void setPropertyOverrides(Map<ResourceLocation, Float> propertyOverrides) {
        this.propertyOverrides = propertyOverrides;
    }

}
