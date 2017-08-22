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

//TODO: Dependencies >.>
// So far we depend on...
// Metamorph [my own custom build at that... need to fix]
@Mod(modid = HardcoreAlchemy.MOD_ID, version = HardcoreAlchemy.VERSION)
public class HardcoreAlchemy
{
    public static final String MOD_ID = "hardcorealchemy";
    public static final String VERSION = "0.1.0";
    public static final String CLIENT_PROXY = "targoss.hardcorealchemy.ClientProxy";
    public static final String COMMON_PROXY = "targoss.hardcorealchemy.CommonProxy";
    
    @SidedProxy(modId=MOD_ID, clientSide=CLIENT_PROXY, serverSide=COMMON_PROXY)
    public static CommonProxy proxy;
    
    public static final Logger LOGGER = LogManager.getLogger("Hardcore Alchemy");
    
    public static final String DISSOLUTION_ID = "dissolution";
    public static boolean isDissolutionLoaded = false;
    
    @EventHandler
    public void preInit(FMLInitializationEvent event) {
        Map<String, ModContainer> modMap = Loader.instance().getIndexedModList();
        ModContainer dissolutionContainer = modMap.get(DISSOLUTION_ID);
        if (dissolutionContainer != null) {
            isDissolutionLoaded = true;
        }
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
}
