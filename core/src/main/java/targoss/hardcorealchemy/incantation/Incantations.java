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

package targoss.hardcorealchemy.incantation;

import static net.minecraft.init.Blocks.STONE;
import static targoss.hardcorealchemy.item.Items.EMPTY_SLATE;
import static targoss.hardcorealchemy.util.InventoryUtil.ITEM_STACK_EMPTY;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.RegistryBuilder;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyPreInit;
import targoss.hardcorealchemy.incantation.api.Incantation;
import targoss.hardcorealchemy.jei.RecipeIncantation;
import targoss.hardcorealchemy.registrar.Registrar;
import targoss.hardcorealchemy.registrar.RegistrarForge;

public class Incantations {
    public static final Registrar<Incantation> INCANTATIONS = new RegistrarForge<Incantation>("incantations", HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    
    public static final IForgeRegistry<Incantation> INCANTATION_REGISTRY = new RegistryBuilder<Incantation>()
            .setName(new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "incantations"))
            .setType(Incantation.class)
            .setIDRange(0, 1024)
            .create();
    
    public static final Incantation INCANTATION_CREATE_SLATE = INCANTATIONS.add("create_slate", new IncantationCreateSlate());
    
    /** Not a real recipe. Used by JEI lookup only. */
    public static final RecipeIncantation RECIPE_INCANTATION_CREATE_SLATE = new RecipeIncantation(Incantations.INCANTATION_CREATE_SLATE, ITEM_STACK_EMPTY, new ItemStack(EMPTY_SLATE), new ItemStack(STONE));
}
