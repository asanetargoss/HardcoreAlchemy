/*
 * Copyright 2017-2018 asanetargoss
 * 
 * This file is part of Hardcore Alchemy.
 * 
 * Hardcore Alchemy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 * 
 * Hardcore Alchemy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hardcore Alchemy.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.listener;

import java.util.Iterator;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandler;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.InventoryExtension;
import targoss.hardcorealchemy.util.InventoryUtil;

public class ListenerInventoryFoodRot extends HardcoreAlchemyListener {
    // Half life of food in ticks (20 days)
    public static final float FOOD_DECAY_HALF_LIFE = 24000.0F * 20.0F;
    // Average rate at which food should decay (quantity per tick)
    public static final float FOOD_DECAY_PER_TICK = 0.5F/FOOD_DECAY_HALF_LIFE;
    // Ticks to wait between each update of the player's inventory (can use Entity.ticksExisted)
    public static final int PLAYER_TICK_INTERVAL = 40;
    // The whole chunk ticks 16 times, so the block check rate keeps cadence with vanilla random block ticks
    public static final int BLOCK_CHECKS_PER_CHUNK = 16;
    // Average amount that food decays each time it is checked in a tile entity inventory slot
    public static final float FOOD_DECAY_TILE_SLOT;
    public static final float FOOD_DECAY_PLAYER_SLOT;
    public static final int MAX_STACK = 64;
    
    static {
        int blocksInChunk = 16*16*256;
        float blockTickRate = 1.0F * (float)BLOCK_CHECKS_PER_CHUNK / (float)blocksInChunk;
        FOOD_DECAY_TILE_SLOT = FOOD_DECAY_PER_TICK/blockTickRate;
        
        float playerTickRate = 1.0F / (float)PLAYER_TICK_INTERVAL;
        FOOD_DECAY_PLAYER_SLOT = FOOD_DECAY_PER_TICK/playerTickRate;
    }
    
    private Random random = new Random();
    private long tileLCG = random.nextLong();
    
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase != Phase.END || event.side != Side.SERVER) {
            return;
        }
        WorldServer world = (WorldServer)(event.world);
        Iterator<Chunk> chunkIterator = world.getPlayerChunkMap().getChunkIterator();
        Iterator<Chunk> actualChunkIterator = ForgeChunkManager.getPersistentChunksIterableFor(world, chunkIterator);
        while (actualChunkIterator.hasNext()) {
            Chunk chunk = actualChunkIterator.next();
            if (chunk != null) {
                updateChunk(chunk);
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != Phase.END || event.side != Side.SERVER) {
            return;
        }
        EntityPlayer player = event.player;
        if (player.ticksExisted % PLAYER_TICK_INTERVAL == 0) {
            for (IItemHandler inventory : InventoryExtension.INSTANCE.getInventories(player)) {
                tickInventory(inventory, FOOD_DECAY_PLAYER_SLOT);
            }
        }
    }
    
    private void updateChunk(Chunk chunk) {
        if (chunk == null) {
            return;
        }
        
        int chunkPosX = chunk.xPosition << 4;
        int chunkPosZ = chunk.zPosition << 4;
        
        for (int i = 0; i < BLOCK_CHECKS_PER_CHUNK; i++) {
            // Constants from MMIX by Donald Knuth
            tileLCG = tileLCG * 6364136223846793005L + 1442695040888963407L;
            int chunkX = (int)(tileLCG & 15);
            int chunkZ = (int)(tileLCG >> 8 & 15);
            int chunkY = (int)(tileLCG >> 16 & 255);
            
            TileEntity tileEntity = chunk.getTileEntity(new BlockPos(chunkPosX+chunkX,chunkY,chunkPosZ+chunkZ), Chunk.EnumCreateEntityType.CHECK);
            if (tileEntity != null) {
                for (IItemHandler inventory : InventoryExtension.INSTANCE.getInventories(tileEntity)) {
                    tickInventory(inventory, FOOD_DECAY_TILE_SLOT);
                }
            }
        }
    }
    
    public static class FoodRotFunc implements InventoryUtil.ItemFunc {
        private Random random = new Random();
        protected float decayRate;
        
        public FoodRotFunc(float decayRate) {
            this.decayRate = decayRate;
        }
        
        @Override
        public boolean apply(IItemHandler inventory, int slot, ItemStack itemStack) {
            if (itemStack.getItem() instanceof ItemFood) {
                int startingFood = itemStack.stackSize;
                // For each item in the stack, there is an average decay rate chance of FOOD_TICK_RATE
                float decayAmount = random.nextFloat() * (float)startingFood * decayRate;
                int decayConstant = (int)Math.floor(decayAmount);
                float decayFraction = decayAmount - decayConstant;
                
                int foodLost = decayConstant;
                foodLost += (random.nextFloat() < decayFraction) ? 1 : 0;
                if (foodLost > 0) {
                    inventory.extractItem(slot, foodLost, false);
                    return true;
                }
            }
            return false;
        }
    }
    
    public boolean tickInventory(IItemHandler inventory, float decayRate) {
        return tickInventory(inventory, decayRate, 0);
    }
    
    /**
     * returns false if no inventory was checked with the given capabilityProvider
     */
    private boolean tickInventory(IItemHandler inventory, float decayRate, int recursionDepth) {
        if (recursionDepth > 6) {
            return false;
        }
        
        if (inventory == null) {
            return false;
        }
        
        // Limit value of decayRate to prevent overflow
        if (decayRate > Integer.MAX_VALUE || decayRate*MAX_STACK > Integer.MAX_VALUE) {
            decayRate = ((float)Integer.MAX_VALUE / (float)MAX_STACK) - 1.0F;
        }
        
        InventoryExtension.INSTANCE.forEachItemRecursive(inventory, new FoodRotFunc(decayRate));
        
        return true;
    }
}
