package targoss.hardcorealchemy.capstone.listener;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.misc.CapabilityMisc;
import targoss.hardcorealchemy.capstone.CapstoneModState;
import targoss.hardcorealchemy.capstone.guide.AlchemicAshGuide;
import targoss.hardcorealchemy.capstone.guide.HCAModpackGuide;
import targoss.hardcorealchemy.capstone.guide.HCAUpgradeGuides;
import targoss.hardcorealchemy.capstone.registrar.RegistrarUpgradeGuide;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;

public class ListenerGuides extends HardcoreAlchemyListener {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        if (!HardcoreAlchemy.proxy.configs.base.enableModpack || !CapstoneModState.isGuideapiLoaded) {
            return;
        }

        HCAModpackGuide.preInit();
        HCAUpgradeGuides.UPGRADE_GUIDES.register(RegistrarUpgradeGuide.BOOK_AND_MODEL);

        if (ModState.isAlchemicAshLoaded) {
            AlchemicAshGuide.preInit();
        }
    }
    @Override
    public void init(FMLInitializationEvent event) {
        if (!HardcoreAlchemy.proxy.configs.base.enableModpack || !CapstoneModState.isGuideapiLoaded) {
            return;
        }
        
        HCAModpackGuide.init();
        HCAUpgradeGuides.UPGRADE_GUIDES.register(RegistrarUpgradeGuide.RECIPES);
        HCAUpgradeGuides.UPGRADE_GUIDES.register(RegistrarUpgradeGuide.CATEGORIES);
        HCAUpgradeGuides.UPGRADE_GUIDES.register(RegistrarUpgradeGuide.CLEANUP);
        // CapabilityMisc currently maintains a guess of the last version from which a player logged in.
        // Here, we change its value, so players in a new world also get the most recent upgrade guide.
        // This behavior may change in the future.
        CapabilityMisc.DEFAULT_EXPECTED_PLAYER_VERSION = HCAUpgradeGuides.UPGRADE_GUIDES.getDefaultExpectedPlayerVersion();
        
        if (ModState.isAlchemicAshLoaded) {
            AlchemicAshGuide.init();
        }
    }
}
