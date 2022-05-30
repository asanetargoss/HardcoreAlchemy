package targoss.hardcorealchemy.modpack;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.misc.CapabilityMisc;
import targoss.hardcorealchemy.modpack.guide.AlchemicAshGuide;
import targoss.hardcorealchemy.modpack.guide.HCAModpackGuide;
import targoss.hardcorealchemy.modpack.guide.HCAUpgradeGuides;
import targoss.hardcorealchemy.modpack.listener.ListenerInventoryExtension;
import targoss.hardcorealchemy.modpack.listener.ListenerPlayerInventory;
import targoss.hardcorealchemy.modpack.listener.ListenerPlayerMagicState;
import targoss.hardcorealchemy.modpack.registrar.RegistrarUpgradeGuide;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerInventory());
        HardcoreAlchemy.proxy.addListener(new ListenerInventoryExtension());
        HardcoreAlchemy.proxy.addListener(new ListenerPlayerMagicState());
        
        if (ModState.isGuideapiLoaded) {
            HCAModpackGuide.preInit();
            HCAUpgradeGuides.UPGRADE_GUIDES.register(RegistrarUpgradeGuide.BOOK_AND_MODEL);
        }
        
        if (ModState.isGuideapiLoaded && ModState.isAlchemicAshLoaded) {
            AlchemicAshGuide.preInit();
        }
    }
    
    public void init(FMLInitializationEvent event) {
        if (ModState.isGuideapiLoaded) {
            HCAModpackGuide.init();
            HCAUpgradeGuides.UPGRADE_GUIDES.register(RegistrarUpgradeGuide.RECIPES);
            HCAUpgradeGuides.UPGRADE_GUIDES.register(RegistrarUpgradeGuide.CATEGORIES);
            HCAUpgradeGuides.UPGRADE_GUIDES.register(RegistrarUpgradeGuide.CLEANUP);
            // CapabilityMisc currently maintains a guess of the last version from which a player logged in.
            // Here, we change its value, so players in a new world also get the most recent upgrade guide.
            // This behavior may change in the future.
            CapabilityMisc.DEFAULT_EXPECTED_PLAYER_VERSION = HCAUpgradeGuides.UPGRADE_GUIDES.getDefaultExpectedPlayerVersion();
        }
        
        if (ModState.isGuideapiLoaded && ModState.isAlchemicAshLoaded) {
            AlchemicAshGuide.init();
        }
    }
}
