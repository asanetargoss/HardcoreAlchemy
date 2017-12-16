package targoss.hardcorealchemy.test.suite;

import java.util.ArrayList;
import java.util.List;

import gr8pefish.ironbackpacks.capabilities.IronBackpacksCapabilities;
import gr8pefish.ironbackpacks.capabilities.player.PlayerWearingBackpackCapabilities;
import gr8pefish.ironbackpacks.container.backpack.InventoryBackpack;
import moze_intel.projecte.gameObjs.tiles.AlchChestTile;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.listener.ListenerInventoryFoodRot;
import targoss.hardcorealchemy.test.HardcoreAlchemyTests;
import targoss.hardcorealchemy.test.api.ITestList;
import targoss.hardcorealchemy.test.api.ITestSuite;
import targoss.hardcorealchemy.test.api.TestList;
import targoss.hardcorealchemy.test.api.UniqueFakePlayer;

public class TestFoodRot implements ITestSuite {
    @Override
    public ITestList getTests() {
        ITestList tests = new TestList();
        boolean projecte = ModState.isProjectELoaded;
        boolean backpacks = ModState.isIronBackpacksLoaded;
        
        tests.put("chest insertion check", this::checkInsertChest);
        tests.put("find chest inventory", this::hasInventoryChest);
        tests.put("decay food in chest", this::decayChest);
        tests.put("decay rate overflow prevention in chest", this::decayChestOverflow);
        
        tests.putIf("find alchemical chest inventory", this::hasInventoryChestPE, projecte);
        
        tests.putIf("find iron backpack inventory", this::hasInventoryIronBackpack, backpacks);
        
        tests.put("get player inventories", this::countPlayerInventories);
        tests.put("decay food in player inventory", this::decayPlayerInventory);
        
        tests.putIf("save iron backpack inventory nbt", this::checkSaveBackpackNbt, backpacks);
        tests.putIf("decay food in backpack in player inventory", this::decayBackpackInPlayerInventory, backpacks);
        tests.putIf("decay food in worn backpack", this::decayWornBackpack, backpacks);
        
        tests.putIf("decay food in alchemical bags", this::decayAlchemicalBags, projecte);
        
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
    
    public boolean hasInventoryChestPE() {
        AlchChestTile chest = new AlchChestTile();
        return ListenerInventoryFoodRot.getInventories(chest).size() == 1;
    }
    
    public static ItemStack createBackpackStack() {
        return new ItemStack(gr8pefish.ironbackpacks.registry.ItemRegistry.basicBackpack);
    }
    
    public boolean hasInventoryIronBackpack() {
        FakePlayer player = UniqueFakePlayer.create();
        ItemStack backpackStack = createBackpackStack();
        
        return ListenerInventoryFoodRot.getInventories(backpackStack).size() == 1;
    }
    
    public boolean countPlayerInventories() {
        FakePlayer player = UniqueFakePlayer.create();
        
        int numInventories = 2;
        if (ModState.isProjectELoaded) {
            numInventories += 16;
        }
        if (ModState.isIronBackpacksLoaded) {
            PlayerWearingBackpackCapabilities backpackCapability = IronBackpacksCapabilities.getWearingBackpackCapability(player);
            backpackCapability.setEquippedBackpack(createBackpackStack());
            numInventories += 1;
        }
        
        return ListenerInventoryFoodRot.getInventories(player).size() == numInventories;
    }
    
    public boolean decayPlayerInventory() {
        FakePlayer player = UniqueFakePlayer.create();
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
        FakePlayer player = UniqueFakePlayer.create();
        player.inventory.setInventorySlotContents(backpackSlot, backpackStack);
        
        tickInventories(ListenerInventoryFoodRot.getInventories(player));
        
        IItemHandler inventoryBackpack2 = new InvWrapper(new InventoryBackpack(player.inventory.getStackInSlot(backpackSlot), true));
        int finalFood = getFoodCount(inventoryBackpack2);
        
        return finalFood < initialFood;
    }
    
    public boolean decayWornBackpack() {
        ItemStack backpackStack = createBackpackStack();
        IItemHandler inventoryBackpack = new InvWrapper(new InventoryBackpack(backpackStack, true));
        
        int initialFood = fillSlotsWithFood(inventoryBackpack);
        ListenerInventoryFoodRot.saveIronBackpackNbt(inventoryBackpack, backpackStack);
        
        FakePlayer player = UniqueFakePlayer.create();
        PlayerWearingBackpackCapabilities backpackCapability = IronBackpacksCapabilities.getWearingBackpackCapability(player);
        backpackCapability.setEquippedBackpack(backpackStack);
        
        tickInventories(ListenerInventoryFoodRot.getInventories(player));
        
        ItemStack backpackStack2 = backpackCapability.getEquippedBackpack();
        IItemHandler inventoryBackpack2 = new InvWrapper(new InventoryBackpack(backpackStack2, true));
        
        int finalFood = getFoodCount(inventoryBackpack2);
        
        return finalFood < initialFood;
    }
    
    public boolean decayAlchemicalBags() {
        FakePlayer player = UniqueFakePlayer.create();
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
