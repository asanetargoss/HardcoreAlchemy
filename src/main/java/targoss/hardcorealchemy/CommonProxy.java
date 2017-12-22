package targoss.hardcorealchemy;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.pam.harvestcraft.blocks.CropRegistry;
import com.pam.harvestcraft.blocks.growables.BlockPamCrop;
import com.pam.harvestcraft.config.ConfigHandler;
import com.pam.harvestcraft.item.ItemRegistry;

import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Optional;
import targoss.hardcorealchemy.capability.combatlevel.CapabilityCombatLevel;
import targoss.hardcorealchemy.capability.food.CapabilityFood;
import targoss.hardcorealchemy.capability.humanity.CapabilityHumanity;
import targoss.hardcorealchemy.capability.killcount.CapabilityKillCount;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.listener.ListenerPlayerHumanity;
import targoss.hardcorealchemy.listener.ListenerPlayerMagic;
import targoss.hardcorealchemy.listener.ListenerPlayerMorph;
import targoss.hardcorealchemy.listener.ListenerBlock;
import targoss.hardcorealchemy.listener.ListenerCrops;
import targoss.hardcorealchemy.listener.ListenerInventoryFoodRot;
import targoss.hardcorealchemy.listener.ListenerMobAI;
import targoss.hardcorealchemy.listener.ListenerMobLevel;
import targoss.hardcorealchemy.listener.ListenerPacketUpdatePlayer;
import targoss.hardcorealchemy.listener.ListenerPlayerDiet;
import targoss.hardcorealchemy.network.PacketHandler;

public class CommonProxy {
    public Configs configs = new Configs();
    
    public void registerListeners() {
        MinecraftForge.EVENT_BUS.register(new ListenerPacketUpdatePlayer(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerPlayerMorph(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerPlayerHumanity(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerPlayerMagic(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerPlayerDiet(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerMobLevel(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerMobAI(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerBlock(configs));
        MinecraftForge.EVENT_BUS.register(new ListenerInventoryFoodRot(configs));
        
        // 1.10-specific tweaks
        MinecraftForge.EVENT_BUS.register(new ListenerCrops(configs));
    }
    
    public void registerCapabilities() {
        CapabilityKillCount.register();
        CapabilityHumanity.register();
        CapabilityCombatLevel.register();
        CapabilityFood.register();
    }
    
    public void registerNetworking() {
        PacketHandler.register();
    }
    
    /**
     * TODO: Do not statically call a listener
     */
    public void postInit() {
        ListenerPlayerHumanity.postInit();
    }
    
    /**
     * Fixes broken config option for Pam's Harvestcraft,
     * so the mod's crops can drop seeds when immature.
     * Called in preInit, so it's before most mod intercompatibility code
     */
    @Optional.Method(modid = ModState.HARVESTCRAFT_ID)
    public void fixPamSeeds() {
        if (!ConfigHandler.cropsdropSeeds) {
            return;
        }
        
        Map<String, BlockPamCrop> allCrops = CropRegistry.getCrops();
        Map<String, Item> allSeeds = CropRegistry.getSeeds();
        
        try {
            Method methodGetSeedName = CropRegistry.class.getDeclaredMethod("getSeedName", String.class);
            methodGetSeedName.setAccessible(true);
            
            Field fieldSeed = BlockPamCrop.class.getDeclaredField("seed");
            fieldSeed.setAccessible(true);
            
            for (Map.Entry<String, BlockPamCrop> cropEntry : allCrops.entrySet()) {
                String cropName = cropEntry.getKey();
                
                BlockPamCrop cropBlock = cropEntry.getValue();
                String actualSeedName = (String)methodGetSeedName.invoke(null, cropName);
                Item actualSeed = ItemRegistry.items.get(actualSeedName);
                
                fieldSeed.set(cropBlock, actualSeed);
                
                allSeeds.put(cropName, actualSeed);
                
            }
        }
        catch (Exception e) {
            HardcoreAlchemy.LOGGER.error("Failed to modify Harvestcraft to make crops drop seeds");
            e.printStackTrace();
        }
    }
}
