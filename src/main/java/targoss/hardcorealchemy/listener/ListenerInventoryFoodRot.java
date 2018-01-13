/**
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

package targoss.hardcorealchemy.listener;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import gr8pefish.ironbackpacks.capabilities.IronBackpacksCapabilities;
import gr8pefish.ironbackpacks.capabilities.player.PlayerWearingBackpackCapabilities;
import gr8pefish.ironbackpacks.container.backpack.InventoryBackpack;
import gr8pefish.ironbackpacks.items.backpacks.ItemBackpack;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.IAlchBagProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.config.Configs;

public class ListenerInventoryFoodRot extends ConfiguredListener {
    public ListenerInventoryFoodRot(Configs configs) {
        super(configs);
    }

    @CapabilityInject(IItemHandler.class)
    public static Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = null;
    
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
    public static final ItemStack NO_ITEM = null;
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
            for (IItemHandler inventory : getInventories(player)) {
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
                for (IItemHandler inventory : getInventories(tileEntity)) {
                    tickInventory(inventory, FOOD_DECAY_TILE_SLOT);
                }
            }
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
        
        // Do decay logic for food, and check for inventories in other items
        int inventorySize = inventory.getSlots();
        for (int i = 0; i < inventorySize; i++) {
            ItemStack itemStack = inventory.getStackInSlot(i);
            
            if (itemStack == null) {
                continue;
            }
            
            if (itemStack.getItem() instanceof ItemFood) {
                int startingFood = itemStack.stackSize;
                // For each item in the stack, there is an average decay rate chance of FOOD_TICK_RATE
                float decayAmount = random.nextFloat() * (float)startingFood * decayRate;
                int decayConstant = (int)Math.floor(decayAmount);
                float decayFraction = decayAmount - decayConstant;
                
                int foodLost = decayConstant;
                foodLost += (random.nextFloat() < decayFraction) ? 1 : 0;
                inventory.extractItem(i, foodLost, false);
            }
            else {
                // Attempt to treat item as if it has an inventory
                for (IItemHandler inventoryStack : getInventories(itemStack)) {
                    tickInventory(inventoryStack, decayRate, recursionDepth+1);
                }
            }
        }
        // Check if this inventory is a backpack item from IronBackpacks, and if so update the item nbt
        //TODO: Figure out what to do if this is a backpack on a player's back
        if (inventory.getClass() == InvWrapper.class) {
            IInventory iInventory = ((InvWrapper)inventory).getInv();
            if (iInventory instanceof InventoryBackpack) {
                ItemStack backpackStack = ((InventoryBackpack)iInventory).getBackpackStack();
                saveIronBackpackNbt(inventory, backpackStack);
            }
        }
        
        return true;
    }
    
    public static void saveIronBackpackNbt(IItemHandler inventory, ItemStack itemStack) {
        // Workaround to prevent NPE in InventoryBackpack.writeToNBT due to null player object
        // We only set the "Items" nbt tag and assume the rest are saved at some other time
        NBTTagCompound nbt = itemStack.getTagCompound();
        if (nbt == null) {
            nbt = new NBTTagCompound();
            itemStack.setTagCompound(nbt);
        }
        NBTTagList inventoryNbt = new NBTTagList();
        nbt.setTag("Items", inventoryNbt);
        
        int slots = inventory.getSlots();
        for (int i = 0; i < slots; i++) {
            ItemStack item = inventory.getStackInSlot(i);
            if (item != NO_ITEM) {
                NBTTagCompound itemNbt = new NBTTagCompound();
                itemNbt.setByte("Slot", (byte)i);
                item.writeToNBT(itemNbt);
                inventoryNbt.appendTag(itemNbt);
            }
        }
    }
    
    @Nonnull
    public static List<IItemHandler> getInventories(@Nonnull ItemStack itemStack) {
        List<IItemHandler> inventories = new ArrayList<>();
        
        // Check if the stack has the item handler capability
        {
            IItemHandler inventory = itemStack.getCapability(ITEM_HANDLER_CAPABILITY, null);
            if (inventory != null) {
                inventories.add(inventory);
            }
        }
        // Check if this is a backpack itemstack from the iron backpacks mod
        if (itemStack.getItem() instanceof ItemBackpack) {
            inventories.add(new InvWrapper(new InventoryBackpack(itemStack, true)));
        }
        
        return inventories;
    }
    
    @Nonnull
    public static List<IItemHandler> getInventories(@Nonnull TileEntity tileEntity) {
        List<IItemHandler> inventories = new ArrayList<>();
        
        IItemHandler inventory = tileEntity.getCapability(ITEM_HANDLER_CAPABILITY, null);
        if (inventory != null) {
            inventories.add(inventory);
        }
        else if (tileEntity instanceof IInventory) {
            inventories.add(new InvWrapper((IInventory)tileEntity));
        }
        
        return inventories;
    }
    
    @Nonnull
    public static List<IItemHandler> getInventories(@Nonnull EntityPlayer player) {
        List<IItemHandler> inventories = new ArrayList<>();
        
        // Player main inventory
        if (player.inventory != null) {
            inventories.add(new InvWrapper(player.inventory));
        }
        // Ender chest inventory
        {
            IInventory inventory = player.getInventoryEnderChest();
            if (inventory != null) {
                inventories.add(new InvWrapper(inventory));
            }
        }
        // Alchemical bag inventories
        if (ModState.isProjectELoaded) {
            inventories.addAll(getAlchemicalBags(player));
        }
        // Equipped backpack inventory
        if (ModState.isIronBackpacksLoaded) {
            PlayerWearingBackpackCapabilities backpackCapability = IronBackpacksCapabilities.getWearingBackpackCapability(player);
            if (backpackCapability != null) {
                ItemStack backpackStack = backpackCapability.getEquippedBackpack();
                if (backpackStack != null) {
                    inventories.add(new InvWrapper(new InventoryBackpack(backpackStack, true)));
                }
            }
        }
        
        return inventories;
    }
    
    @Nonnull
    @Optional.Method(modid = ModState.PROJECT_E_ID)
    public static List<IItemHandler> getAlchemicalBags(@Nonnull EntityPlayer player) {
        List<IItemHandler> inventories = new ArrayList<>();
        
        IAlchBagProvider alchBags = player.getCapability(ProjectEAPI.ALCH_BAG_CAPABILITY, null);
        if (alchBags != null) {
            for (EnumDyeColor dyeColor : EnumSet.allOf(EnumDyeColor.class)) {
                inventories.add(alchBags.getBag(dyeColor));
            }
        }
        
        return inventories;
    }
}
