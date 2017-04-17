package targoss.hardcorealchemy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

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
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        //TODO: Console logger >.>
        System.out.println("It's time to get magical.");
        proxy.registerListeners();
        proxy.registerCapabilities();
        proxy.registerNetworking();
    }
}
