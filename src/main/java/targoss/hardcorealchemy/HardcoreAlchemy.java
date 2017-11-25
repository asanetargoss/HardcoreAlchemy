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
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import targoss.hardcorealchemy.command.CommandTest;
import targoss.hardcorealchemy.test.HardcoreAlchemyTests;

@Mod(modid = HardcoreAlchemy.MOD_ID, version = HardcoreAlchemy.VERSION,
    dependencies = HardcoreAlchemy.DEPENDENCIES, acceptedMinecraftVersions = HardcoreAlchemy.MC_VERSIONS)
public class HardcoreAlchemy
{
    public static final String MOD_ID = "hardcorealchemy";
    public static final String VERSION = "0.1.0";
    public static final String DEPENDENCIES = "required-after:metamorph;";
    public static final String MC_VERSIONS = "[1.10.2]";
    public static final String CLIENT_PROXY = "targoss.hardcorealchemy.ClientProxy";
    public static final String COMMON_PROXY = "targoss.hardcorealchemy.CommonProxy";
    
    @SidedProxy(modId=MOD_ID, clientSide=CLIENT_PROXY, serverSide=COMMON_PROXY)
    public static CommonProxy proxy;
    
    public static final Logger LOGGER = LogManager.getLogger("Hardcore Alchemy");
    
    //TODO: move mod constants to their own file
    public static final String DISSOLUTION_ID = "dissolution";
    public static final String NUTRITION_ID = "nutrition";
    public static final String BLOOD_MAGIC_ID = "BloodMagic";
    public static final String ARS_MAGICA_ID = "arsmagica2";
    public static final String PROJECT_E_ID = "ProjectE";
    public static final String IRON_BACKPACKS_ID = "ironbackpacks";
    public static final String TAN_ID = "ToughAsNails";
    public static boolean isDissolutionLoaded = false;
    public static boolean isNutritionLoaded = false;
    public static boolean isBloodMagicLoaded = false;
    public static boolean isArsMagicaLoaded = false;
    public static boolean isProjectELoaded = false;
    public static boolean isIronBackpacksLoaded = false;
    public static boolean isTanLoaded = false;
    
    //TODO: Why isn't this FMLPreInitializationEvent?
    @EventHandler
    public void preInit(FMLInitializationEvent event) {
        Map<String, ModContainer> modMap = Loader.instance().getIndexedModList();
        isDissolutionLoaded = modMap.containsKey(DISSOLUTION_ID);
        isNutritionLoaded = modMap.containsKey(NUTRITION_ID);
        isBloodMagicLoaded = modMap.containsKey(BLOOD_MAGIC_ID);
        isArsMagicaLoaded = modMap.containsKey(ARS_MAGICA_ID);
        isProjectELoaded = modMap.containsKey(PROJECT_E_ID);
        isIronBackpacksLoaded = modMap.containsKey(IRON_BACKPACKS_ID);
        isTanLoaded = modMap.containsKey(TAN_ID);
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
