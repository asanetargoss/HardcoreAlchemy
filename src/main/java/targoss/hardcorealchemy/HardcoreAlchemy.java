package targoss.hardcorealchemy;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import targoss.hardcorealchemy.command.CommandTest;
import targoss.hardcorealchemy.test.HardcoreAlchemyTests;

@Mod(modid = HardcoreAlchemy.MOD_ID, version = HardcoreAlchemy.VERSION,
    dependencies = HardcoreAlchemy.DEPENDENCIES, acceptedMinecraftVersions = HardcoreAlchemy.MC_VERSIONS)
public class HardcoreAlchemy
{
    public static final String MOD_ID = "hardcorealchemy";
    public static final String VERSION = "0.2.1";
    public static final String DEPENDENCIES = "required-after:metamorph;";
    public static final String MC_VERSIONS = "[1.10.2]";
    public static final String CLIENT_PROXY = "targoss.hardcorealchemy.ClientProxy";
    public static final String COMMON_PROXY = "targoss.hardcorealchemy.CommonProxy";
    
    @SidedProxy(modId=MOD_ID, clientSide=CLIENT_PROXY, serverSide=COMMON_PROXY)
    public static CommonProxy proxy;
    
    public static Logger LOGGER = null;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
                
        Map<String, ModContainer> modMap = Loader.instance().getIndexedModList();
        ModState.isDissolutionLoaded = modMap.containsKey(ModState.DISSOLUTION_ID);
        ModState.isNutritionLoaded = modMap.containsKey(ModState.NUTRITION_ID);
        ModState.isBloodMagicLoaded = modMap.containsKey(ModState.BLOOD_MAGIC_ID);
        ModState.isArsMagicaLoaded = modMap.containsKey(ModState.ARS_MAGICA_ID);
        ModState.isProjectELoaded = modMap.containsKey(ModState.PROJECT_E_ID);
        ModState.isIronBackpacksLoaded = modMap.containsKey(ModState.IRON_BACKPACKS_ID);
        ModState.isTanLoaded = modMap.containsKey(ModState.TAN_ID);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        LOGGER.info("It's time to get magical.");
        
        proxy.registerListeners();
        proxy.registerCapabilities();
        proxy.registerNetworking();
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandTest());
        HardcoreAlchemyTests.setServerForEvent(event);
    }
    
    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        HardcoreAlchemyTests.setServerForEvent(event);
    }
}
