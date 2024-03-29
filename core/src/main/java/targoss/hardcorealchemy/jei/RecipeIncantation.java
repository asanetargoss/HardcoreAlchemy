/*
 * Copyright 2017-2023 asanetargoss
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

package targoss.hardcorealchemy.jei;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import targoss.hardcorealchemy.incantation.api.Incantation;

public class RecipeIncantation {
    public final Incantation incantation;
    public final ItemStack input;
    public final ItemStack output;
    public final ItemStack interact;
    
    public RecipeIncantation(Incantation incantation, @Nullable ItemStack input, @Nullable ItemStack output, @Nullable ItemStack interact) {
        this.incantation = incantation;
        this.input = input;
        this.output = output;
        this.interact = interact;
    }
}
