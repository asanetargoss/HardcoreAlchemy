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

package targoss.hardcorealchemy.creatures.block;

import static net.minecraft.init.Items.IRON_INGOT;
import static net.minecraft.init.Items.QUARTZ;
import static targoss.hardcorealchemy.block.Blocks.BLOCKS;
import static targoss.hardcorealchemy.block.Blocks.BLOCK_MODELS;
import static targoss.hardcorealchemy.block.Blocks.ORE_DIMENSIONAL_FLUX_CRYSTAL;
import static targoss.hardcorealchemy.block.Blocks.TILE_ENTITIES;
import static targoss.hardcorealchemy.block.Blocks.Client.CLIENT_BLOCK_MODELS;
import static targoss.hardcorealchemy.item.Items.EMPTY_SLATE;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.block.BlockModelInfo;
import targoss.hardcorealchemy.block.TileEntityInfo;

public class Blocks {
    public static final Block BLOCK_HUMANITY_PHYLACTERY = BLOCKS.add("alchemist_core", new BlockHumanityPhylactery());
    
    public static final TileEntityInfo<TileHumanityPhylactery> TILE_HUMANITY_PHYLACTERY = TILE_ENTITIES.add("alchemist_core", new TileEntityInfo<TileHumanityPhylactery>(TileHumanityPhylactery.class));
    
    public static final ResourceLocation PARTICLES_HUMANITY_PHYLACTERY = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "items/alchemist_core");
    public static final BlockModelInfo<TileHumanityPhylactery> MODEL_HUMANITY_PHYLACTERY_OUTER_FRAME = BLOCK_MODELS.add("alchemist_core_outer_frame", new BlockModelInfo<TileHumanityPhylactery>(BLOCK_HUMANITY_PHYLACTERY, TILE_HUMANITY_PHYLACTERY.clazz).setCustomParticleTexture(PARTICLES_HUMANITY_PHYLACTERY));
    public static final BlockModelInfo<TileHumanityPhylactery> MODEL_HUMANITY_PHYLACTERY_INNER_FRAME = BLOCK_MODELS.add("alchemist_core_inner_frame", new BlockModelInfo<TileHumanityPhylactery>(BLOCK_HUMANITY_PHYLACTERY, TILE_HUMANITY_PHYLACTERY.clazz).setCustomParticleTexture(PARTICLES_HUMANITY_PHYLACTERY));
    
    public static class Client {
        public static final BlockModelInfo.Client<TileHumanityPhylactery> CLIENT_MODEL_HUMANITY_PHYLACTERY = CLIENT_BLOCK_MODELS.add("alchemist_core", new BlockModelInfo.Client<TileHumanityPhylactery>(MODEL_HUMANITY_PHYLACTERY_OUTER_FRAME, new TESRHumanityPhylactery()));
    }
    
    public static void registerRecipes() {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(BLOCK_HUMANITY_PHYLACTERY), new Object[] {
                /* Mirrored */ true,
                "SII",
                "ICI",
                "IIS",
                'I', new ItemStack(IRON_INGOT),
                'C', ORE_DIMENSIONAL_FLUX_CRYSTAL,
                'S', EMPTY_SLATE
        }));
    }
    
    public static void registerCompat() {
        if (OreDictionary.getOres(ORE_DIMENSIONAL_FLUX_CRYSTAL).isEmpty()) {
            OreDictionary.registerOre(ORE_DIMENSIONAL_FLUX_CRYSTAL, QUARTZ);
        }
    }
}
