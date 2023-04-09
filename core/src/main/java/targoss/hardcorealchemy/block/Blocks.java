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

package targoss.hardcorealchemy.block;

import net.minecraft.block.Block;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.coremod.HardcoreAlchemyPreInit;
import targoss.hardcorealchemy.registrar.Registrar;
import targoss.hardcorealchemy.registrar.RegistrarBlock;
import targoss.hardcorealchemy.registrar.RegistrarBlockModel;
import targoss.hardcorealchemy.registrar.RegistrarTileEntity;

public class Blocks {
    public static final Registrar<Block> BLOCKS = new RegistrarBlock("blocks", HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    public static final Registrar<TileEntityInfo<?>> TILE_ENTITIES = new RegistrarTileEntity("tile_entities", HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    public static final Registrar<BlockModelInfo<?>> BLOCK_MODELS = new RegistrarBlockModel("block_models", HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    
    public static final String ORE_DIMENSIONAL_FLUX_CRYSTAL = HardcoreAlchemyCore.MOD_ID + ":dimensional_flux_crystal";
    
    public static class Client {
        public static final Registrar<BlockModelInfo.Client<?>> CLIENT_BLOCK_MODELS = new RegistrarBlockModel.Client("client_block_models", HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    }
}
