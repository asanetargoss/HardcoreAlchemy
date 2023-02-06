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
    
    public static class Client {
        public static final Registrar<BlockModelInfo.Client<?>> CLIENT_BLOCK_MODELS = new RegistrarBlockModel.Client("client_block_models", HardcoreAlchemyCore.MOD_ID, HardcoreAlchemyPreInit.LOGGER);
    }
}
