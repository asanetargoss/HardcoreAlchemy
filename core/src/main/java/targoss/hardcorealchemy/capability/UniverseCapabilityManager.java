package targoss.hardcorealchemy.capability;

import java.util.ArrayList;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import targoss.hardcorealchemy.util.WorldUtil;

/**
 * Handles the full lifecycle for global server data
 */
public class UniverseCapabilityManager {
    public static UniverseCapabilityManager INSTANCE = new UniverseCapabilityManager(CapabilityManager.INSTANCE);
    
    protected CapabilityManager forgeCaps;
    
    protected static class UniverseCap {
        public ResourceLocation key;
        public ICapabilityProvider cap;
        
        public UniverseCap(ResourceLocation key, ICapabilityProvider cap) {
            this.key = key;
            this.cap = cap;
        }
    }
    protected ArrayList<UniverseCap> universeCaps;
    
    public UniverseCapabilityManager(CapabilityManager forgeCaps) {
        this.forgeCaps = forgeCaps;
    }
    
    /** Capabilities registered here will automatically be attached via the attachCapabilitiesEvent */
    public void register(ResourceLocation key, ICapabilityProvider cap) {
        UniverseCap ucap = new UniverseCap(key, cap);
        universeCaps.add(ucap);
    }
    
    public void maybeAttachCapabilities(AttachCapabilitiesEvent<World> event) {
        World eventWorld = event.getObject();
        World overworld = WorldUtil.getOverworld(eventWorld);
        if (eventWorld != overworld) {
            return;
        }
        for (UniverseCap ucap : universeCaps) {
            event.addCapability(ucap.key, ucap.cap);
        }
    }

    public <T> T get(World dummyWorld, Capability<T> capability) {
        World overworld = WorldUtil.getOverworld(dummyWorld);
        T instance = overworld.getCapability(capability, null);
        return instance;
    }
}
