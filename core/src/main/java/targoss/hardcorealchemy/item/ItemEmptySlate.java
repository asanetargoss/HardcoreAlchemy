/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemEmptySlate extends Item {
    public static boolean enchantmentEnabled = false;

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return enchantmentEnabled;
    }

    @Override
    public int getItemEnchantability(ItemStack stack) {
        if (!enchantmentEnabled) {
            return 0;
        }
        return ToolMaterial.IRON.getEnchantability();
    }
}
