package targoss.hardcorealchemy.listener;

import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.capability.UniverseCapabilityManager;

public class ListenerUniverseCapabilities extends HardcoreAlchemyListener {
    @SubscribeEvent
    void onAttachCapabilities(@SuppressWarnings("deprecation") AttachCapabilitiesEvent.World event) {
        UniverseCapabilityManager.INSTANCE.maybeAttachCapabilities(event);
    }
}
