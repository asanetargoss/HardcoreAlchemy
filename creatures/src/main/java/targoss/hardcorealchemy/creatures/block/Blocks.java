package targoss.hardcorealchemy.creatures.block;

import static targoss.hardcorealchemy.block.Blocks.BLOCKS;
import static targoss.hardcorealchemy.block.Blocks.BLOCK_MODELS;
import static targoss.hardcorealchemy.block.Blocks.TILE_ENTITIES;
import static targoss.hardcorealchemy.block.Blocks.Client.CLIENT_BLOCK_MODELS;

import net.minecraft.block.Block;
import targoss.hardcorealchemy.block.BlockModelInfo;
import targoss.hardcorealchemy.block.TileEntityInfo;

public class Blocks {
    public static final Block BLOCK_HUMANITY_PHYLACTERY = BLOCKS.add("humanity_phylactery", new BlockHumanityPhylactery());
    
    public static final TileEntityInfo<TileHumanityPhylactery> TILE_HUMANITY_PHYLACTERY = TILE_ENTITIES.add("humanity_phlactery", new TileEntityInfo<TileHumanityPhylactery>(TileHumanityPhylactery.class));
    
    public static final BlockModelInfo<TileHumanityPhylactery> MODEL_HUMANITY_PHYLACTERY = BLOCK_MODELS.add("humanity_phylactery", new BlockModelInfo<TileHumanityPhylactery>(BLOCK_HUMANITY_PHYLACTERY, TILE_HUMANITY_PHYLACTERY.clazz));
    
    public static class Client {
        public static final BlockModelInfo.Client<TileHumanityPhylactery> CLIENT_MODEL_HUMANITY_PHYLACTERY = CLIENT_BLOCK_MODELS.add("humanity_phylactery", new BlockModelInfo.Client<TileHumanityPhylactery>(MODEL_HUMANITY_PHYLACTERY, new TESRHumanityPhylactery()));
    }
}
