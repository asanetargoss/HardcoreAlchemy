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

import static targoss.hardcorealchemy.block.Blocks.BLOCKS;
import static targoss.hardcorealchemy.block.Blocks.BLOCK_MODELS;
import static targoss.hardcorealchemy.block.Blocks.TILE_ENTITIES;
import static targoss.hardcorealchemy.block.Blocks.Client.CLIENT_BLOCK_MODELS;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.block.BlockModelInfo;
import targoss.hardcorealchemy.block.TileEntityInfo;

public class Blocks {
    public static final Block BLOCK_HUMANITY_PHYLACTERY = BLOCKS.add("humanity_phylactery", new BlockHumanityPhylactery());
    
    public static final TileEntityInfo<TileHumanityPhylactery> TILE_HUMANITY_PHYLACTERY = TILE_ENTITIES.add("humanity_phlactery", new TileEntityInfo<TileHumanityPhylactery>(TileHumanityPhylactery.class));
    
    public static final ResourceLocation PARTICLES_HUMANITY_PHYLACTERY = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "items/alchemist_core");
    public static final BlockModelInfo<TileHumanityPhylactery> MODEL_HUMANITY_PHYLACTERY_OUTER_FRAME = BLOCK_MODELS.add("humanity_phylactery_outer_frame", new BlockModelInfo<TileHumanityPhylactery>(BLOCK_HUMANITY_PHYLACTERY, TILE_HUMANITY_PHYLACTERY.clazz).setCustomParticleTexture(PARTICLES_HUMANITY_PHYLACTERY));
    public static final BlockModelInfo<TileHumanityPhylactery> MODEL_HUMANITY_PHYLACTERY_INNER_FRAME = BLOCK_MODELS.add("humanity_phylactery_inner_frame", new BlockModelInfo<TileHumanityPhylactery>(BLOCK_HUMANITY_PHYLACTERY, TILE_HUMANITY_PHYLACTERY.clazz).setCustomParticleTexture(PARTICLES_HUMANITY_PHYLACTERY));
    
    public static class Client {
        public static final BlockModelInfo.Client<TileHumanityPhylactery> CLIENT_MODEL_HUMANITY_PHYLACTERY = CLIENT_BLOCK_MODELS.add("humanity_phylactery", new BlockModelInfo.Client<TileHumanityPhylactery>(MODEL_HUMANITY_PHYLACTERY_OUTER_FRAME, new TESRHumanityPhylactery()));
    }
}
