package targoss.hardcorealchemy.listener;

import net.minecraftforge.common.capabilities.CapabilityManager;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.capability.combatlevel.CapabilityCombatLevel;
import targoss.hardcorealchemy.capability.combatlevel.ICapabilityCombatLevel;
import targoss.hardcorealchemy.capability.combatlevel.StorageCombatLevel;
import targoss.hardcorealchemy.capability.dimensionhistory.CapabilityDimensionHistory;
import targoss.hardcorealchemy.capability.dimensionhistory.ICapabilityDimensionHistory;
import targoss.hardcorealchemy.capability.dimensionhistory.ProviderDimensionHistory;
import targoss.hardcorealchemy.capability.dimensionhistory.StorageDimensionHistory;
import targoss.hardcorealchemy.capability.entitystate.CapabilityEntityState;
import targoss.hardcorealchemy.capability.entitystate.ICapabilityEntityState;
import targoss.hardcorealchemy.capability.entitystate.StorageEntityState;
import targoss.hardcorealchemy.capability.food.CapabilityFood;
import targoss.hardcorealchemy.capability.food.ICapabilityFood;
import targoss.hardcorealchemy.capability.food.StorageFood;
import targoss.hardcorealchemy.capability.humanity.CapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.StorageHumanity;
import targoss.hardcorealchemy.capability.inactive.IInactiveCapabilities;
import targoss.hardcorealchemy.capability.inactive.InactiveCapabilities;
import targoss.hardcorealchemy.capability.inactive.StorageInactiveCapabilities;
import targoss.hardcorealchemy.capability.instinct.CapabilityInstinct;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.capability.instinct.StorageInstinct;
import targoss.hardcorealchemy.capability.killcount.CapabilityKillCount;
import targoss.hardcorealchemy.capability.killcount.ICapabilityKillCount;
import targoss.hardcorealchemy.capability.killcount.StorageKillCount;
import targoss.hardcorealchemy.capability.misc.CapabilityMisc;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;
import targoss.hardcorealchemy.capability.misc.StorageMisc;
import targoss.hardcorealchemy.capability.morphstate.CapabilityMorphState;
import targoss.hardcorealchemy.capability.morphstate.ICapabilityMorphState;
import targoss.hardcorealchemy.capability.morphstate.StorageMorphState;
import targoss.hardcorealchemy.capability.research.CapabilityResearch;
import targoss.hardcorealchemy.capability.research.ICapabilityResearch;
import targoss.hardcorealchemy.capability.research.StorageResearch;
import targoss.hardcorealchemy.capability.serverdata.CapabilityServerData;
import targoss.hardcorealchemy.capability.serverdata.ICapabilityServerData;
import targoss.hardcorealchemy.capability.serverdata.StorageServerData;
import targoss.hardcorealchemy.capability.tilehistory.CapabilityTileHistory;
import targoss.hardcorealchemy.capability.tilehistory.ICapabilityTileHistory;
import targoss.hardcorealchemy.capability.tilehistory.StorageTileHistory;

public class ListenerCapabilities extends HardcoreAlchemyListener {
    
    @Override
    public void registerCapabilities(CapabilityManager manager, CapUtil.Manager virtualManager) {
        manager.register(ICapabilityKillCount.class, new StorageKillCount(), CapabilityKillCount.class);
        manager.register(ICapabilityHumanity.class, new StorageHumanity(), CapabilityHumanity.class);
        manager.register(ICapabilityCombatLevel.class, new StorageCombatLevel(), CapabilityCombatLevel.class);
        manager.register(ICapabilityFood.class, new StorageFood(), CapabilityFood.class);
        virtualManager.registerVirtualCapability(CapabilityFood.RESOURCE_LOCATION, CapabilityFood.FOOD_CAPABILITY);
        manager.register(ICapabilityServerData.class, new StorageServerData(), CapabilityServerData.class);
        manager.register(IInactiveCapabilities.class, new StorageInactiveCapabilities(), InactiveCapabilities.class);
        manager.register(ICapabilityMorphState.class, new StorageMorphState(), CapabilityMorphState.class);
        manager.register(ICapabilityInstinct.class, new StorageInstinct(), CapabilityInstinct.class);
        manager.register(ICapabilityMisc.class, new StorageMisc(), CapabilityMisc.class);
        manager.register(ICapabilityEntityState.class, new StorageEntityState(), CapabilityEntityState.class);
        manager.register(ICapabilityTileHistory.class, new StorageTileHistory(), CapabilityTileHistory.class);
        manager.register(ICapabilityResearch.class, new StorageResearch(), CapabilityResearch.class);
        manager.register(ICapabilityDimensionHistory.class, new StorageDimensionHistory(), CapabilityDimensionHistory.class);
        virtualManager.registerVirtualCapability(CapabilityDimensionHistory.RESOURCE_LOCATION, ProviderDimensionHistory.DIMENSION_HISTORY_CAPABILITY);
    }

}
