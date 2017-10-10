package targoss.hardcorealchemy.test.suite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gr8pefish.ironbackpacks.capabilities.IronBackpacksCapabilities;
import gr8pefish.ironbackpacks.capabilities.player.PlayerWearingBackpackCapabilities;
import gr8pefish.ironbackpacks.container.backpack.InventoryBackpack;
import moze_intel.projecte.gameObjs.tiles.AlchChestTile;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.listener.ListenerInventoryFoodRot;
import targoss.hardcorealchemy.test.HardcoreAlchemyTests;
import targoss.hardcorealchemy.test.api.ITestSuite;
import targoss.hardcorealchemy.test.api.Test;

public class TestFoodRot implements ITestSuite {
    @Override
    public Map<String, Test> getTests() {
        Map<String, Test> tests = new HashMap<>();
        
        tests.put("chest insertion check", this::checkInsertChest);
        tests.put("find chest inventory", this::hasInventoryChest);
        tests.put("decay food in chest", this::decayChest);
        tests.put("decay rate overflow prevention in chest", this::decayChestOverflow);
        if (HardcoreAlchemy.isProjectELoaded) {
            tests.put("find alchemical chest inventory", this::hasInventoryChestPE);
        }
        tests.put("check server reference", this::checkServerTestReference);
        tests.put("check overworld available", this::checkOverworldAvailable);
        if (HardcoreAlchemy.isIronBackpacksLoaded) {
            tests.put("find iron backpack inventory", this::hasInventoryIronBackpack);
        }
        tests.put("get player inventories", this::countPlayerInventories);
        tests.put("decay food in player inventory", this::decayPlayerInventory);
        if (HardcoreAlchemy.isIronBackpacksLoaded) {
            tests.put("save iron backpack inventory nbt", this::checkSaveBackpackNbt);
            tests.put("decay food in backpack in player inventory", this::decayBackpackInPlayerInventory);
            tests.put("decay food in worn backpack", this::decayWornBackpack);
        }
        if (HardcoreAlchemy.isProjectELoaded) {
            tests.put("decay food in alchemical bags", this::decayAlchemicalBags);
        }
        
        return tests;
    }
    
    @CapabilityInject(IItemHandler.class)
    public static Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = null;
    public static final ItemStack NO_ITEM = null;
    
    public static ItemStack createFood() {
        return new ItemStack(Item.getByNameOrId("minecraft:apple"));
    }
    
    public boolean checkInsertChest() {
        TileEntityChest chest = new TileEntityChest();
        IItemHandler inventory = chest.getCapability(ITEM_HANDLER_CAPABILITY, null);
        int slots = inventory.getSlots();
        
        for (int i=0; i<slots; i++) {
            inventory.insertItem(i, createFood(), false);
            if (inventory.getStackInSlot(i).stackSize != 1) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean hasInventoryChest() {
        TileEntityChest chest = new TileEntityChest();
        return ListenerInventoryFoodRot.getInventories(chest).size() == 1;
    }
    
    /**
     * returns number of food items
     */
    public static int fillSlotsWithFood(IItemHandler inventory) {
        int slots = inventory.getSlots();
        int initialFood = slots;
        for (int i=0; i<slots; i++) {
            inventory.insertItem(i, createFood(), false);
        }
        return initialFood;
    }
    
    public static void tickInventories(List<IItemHandler> inventories) {
        tickInventories(1.0E5F, inventories);
    }
    
    public static void tickInventories(float decayRate, List<IItemHandler> inventories) {
        ListenerInventoryFoodRot listener = new ListenerInventoryFoodRot();
        for (IItemHandler itemHandler : inventories) {
            listener.tickInventory(itemHandler, decayRate);
        }
    }
    
    public static int getFoodCount(IItemHandler inventory) {
        int slots = inventory.getSlots();
        int foodCount = 0;
        for (int i=0; i<slots; i++) {
            if (inventory.getStackInSlot(i) != NO_ITEM) {
                foodCount += inventory.getStackInSlot(i).stackSize;
            }
        }
        return foodCount;
    }
    
    public static boolean decayChestAtRate(float decayRate) {
        TileEntityChest chest = new TileEntityChest();
        IItemHandler inventory = chest.getCapability(ITEM_HANDLER_CAPABILITY, null);
        int slots = inventory.getSlots();
        
        int initialFood = fillSlotsWithFood(inventory);
        
        tickInventories(decayRate, ListenerInventoryFoodRot.getInventories(chest));
        
        int finalFood = getFoodCount(inventory);
        
        return finalFood < initialFood;
    }
    
    public boolean decayChest() {
        return decayChestAtRate(1.0E5F);
    }
    
    public boolean decayChestOverflow() {
        return decayChestAtRate(1.0E30F);
    }
    
    @Optional.Method(modid = HardcoreAlchemy.PROJECT_E_ID)
    public boolean hasInventoryChestPE() {
        AlchChestTile chest = new AlchChestTile();
        return ListenerInventoryFoodRot.getInventories(chest).size() == 1;
    }
    
    public boolean checkServerTestReference() {
        return HardcoreAlchemyTests.SERVER_REFERENCE != null && HardcoreAlchemyTests.SERVER_REFERENCE.get() != null;
    }
    
    public boolean checkOverworldAvailable() {
        MinecraftServer server = HardcoreAlchemyTests.SERVER_REFERENCE.get();
        WorldServer worldServer = server.worldServerForDimension(DimensionType.OVERWORLD.getId());
        return worldServer != null;
    }
    
    public static FakePlayer createPlayer() {
        MinecraftServer server = HardcoreAlchemyTests.SERVER_REFERENCE.get();
        WorldServer worldServer = server.worldServerForDimension(DimensionType.OVERWORLD.getId());
        
        return FakePlayerFactory.getMinecraft(worldServer);
    }
    
    public static ItemStack createBackpackStack() {
        return new ItemStack(gr8pefish.ironbackpacks.registry.ItemRegistry.basicBackpack);
    }
    
    @Optional.Method(modid = HardcoreAlchemy.IRON_BACKPACKS_ID)
    public boolean hasInventoryIronBackpack() {
        FakePlayer player = createPlayer();
        ItemStack backpackStack = createBackpackStack();
        
        return ListenerInventoryFoodRot.getInventories(backpackStack).size() == 1;
    }
    
    public boolean countPlayerInventories() {
        FakePlayer player = createPlayer();
        
        int numInventories = 2;
        if (HardcoreAlchemy.isProjectELoaded) {
            numInventories += 16;
        }
        if (HardcoreAlchemy.isIronBackpacksLoaded) {
            PlayerWearingBackpackCapabilities backpackCapability = IronBackpacksCapabilities.getWearingBackpackCapability(player);
            backpackCapability.setEquippedBackpack(createBackpackStack());
            numInventories += 1;
        }
        
        return ListenerInventoryFoodRot.getInventories(player).size() == numInventories;
    }
    
    public boolean decayPlayerInventory() {
        FakePlayer player = createPlayer();
        IItemHandler playerInventory = new InvWrapper(player.inventory);
        
        int initialFood = fillSlotsWithFood(playerInventory);
        
        tickInventories(ListenerInventoryFoodRot.getInventories(player));
        
        int finalFood = getFoodCount(playerInventory);
        
        return finalFood < initialFood;
    }
    
    public boolean checkSaveBackpackNbt() {
        ItemStack backpackStack = createBackpackStack();
        /* I can't reference IInventory methods directly from
         * an InventoryBackpack instance without breaking compilation,
         * because the Iron Backpacks jar is obfuscated.
         */
        InventoryBackpack inventoryBackpack = new InventoryBackpack(backpackStack, true);
        IInventory iInventoryBackpack = inventoryBackpack;
        int foodSlot = 0;
        iInventoryBackpack.setInventorySlotContents(foodSlot, createFood());
        ListenerInventoryFoodRot.saveIronBackpackNbt(new InvWrapper(inventoryBackpack), backpackStack);
        
        IInventory inventoryBackpack2 = new InventoryBackpack(inventoryBackpack.getBackpackStack(), true);
        return inventoryBackpack2.getStackInSlot(foodSlot) != NO_ITEM;
    }
    
    public boolean decayBackpackInPlayerInventory() {
        ItemStack backpackStack = createBackpackStack();
        IItemHandler inventoryBackpack = new InvWrapper(new InventoryBackpack(backpackStack, true));
        
        int initialFood = fillSlotsWithFood(inventoryBackpack);
        ListenerInventoryFoodRot.saveIronBackpackNbt(inventoryBackpack, backpackStack);
        
        int backpackSlot = 0;
        FakePlayer player = createPlayer();
        player.inventory.setInventorySlotContents(backpackSlot, backpackStack);
        
        tickInventories(ListenerInventoryFoodRot.getInventories(player));
        
        IItemHandler inventoryBackpack2 = new InvWrapper(new InventoryBackpack(player.inventory.getStackInSlot(backpackSlot), true));
        int finalFood = getFoodCount(inventoryBackpack2);
        
        return finalFood < initialFood;
    }
    
    @Optional.Method(modid = HardcoreAlchemy.IRON_BACKPACKS_ID)
    public boolean decayWornBackpack() {
        ItemStack backpackStack = createBackpackStack();
        IItemHandler inventoryBackpack = new InvWrapper(new InventoryBackpack(backpackStack, true));
        
        int initialFood = fillSlotsWithFood(inventoryBackpack);
        ListenerInventoryFoodRot.saveIronBackpackNbt(inventoryBackpack, backpackStack);
        
        FakePlayer player = createPlayer();
        PlayerWearingBackpackCapabilities backpackCapability = IronBackpacksCapabilities.getWearingBackpackCapability(player);
        backpackCapability.setEquippedBackpack(backpackStack);
        
        tickInventories(ListenerInventoryFoodRot.getInventories(player));
        
        ItemStack backpackStack2 = backpackCapability.getEquippedBackpack();
        IItemHandler inventoryBackpack2 = new InvWrapper(new InventoryBackpack(backpackStack2, true));
        
        int finalFood = getFoodCount(inventoryBackpack2);
        
        return finalFood < initialFood;
    }
    
    @Optional.Method(modid = HardcoreAlchemy.PROJECT_E_ID)
    public boolean decayAlchemicalBags() {
        FakePlayer player = createPlayer();
        List<IItemHandler> alchemicalBags = ListenerInventoryFoodRot.getAlchemicalBags(player);
        IItemHandler alchemicalBag = alchemicalBags.get(0);
        
        int initialFood = fillSlotsWithFood(alchemicalBag);
        
        List<IItemHandler> testBags = new ArrayList<>();
        testBags.add(alchemicalBag);
        tickInventories(ListenerInventoryFoodRot.getInventories(player));
        
        int finalFood = getFoodCount(alchemicalBag);
        
        return finalFood < initialFood;
    }
}
