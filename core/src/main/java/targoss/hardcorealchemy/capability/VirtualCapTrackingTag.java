package targoss.hardcorealchemy.capability;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

/**
 * Keeps track of virtual capabilities that have been deserialized
 */
public class VirtualCapTrackingTag extends NBTTagCompound implements ICapabilityProvider {
    static final String KEY = "VirtualCaps";
    
    protected ICapabilityProvider[] providers;

    VirtualCapTrackingTag() {
        super();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (providers == null) {
            return false;
        }
        for (int i = 0; i < providers.length; ++i) {
            ICapabilityProvider provider = providers[i];
            if (provider.hasCapability(capability, facing)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (providers == null) {
            return null;
        }
        for (int i = 0; i < providers.length; ++i) {
            ICapabilityProvider provider = providers[i];
            T t = provider.getCapability(capability, facing);
            if (t != null) {
                return t;
            }
        }
        return null;
    }
    
    public void addProvider(ICapabilityProvider provider) {
        if (providers == null) {
            providers = new ICapabilityProvider[] { provider };
        } else {
            providers = Arrays.copyOf(providers, providers.length + 1);
            providers[providers.length - 1] = provider;
        }
    }
    
    public <T> void addOrReplaceProvider(Capability<T> capability, ICapabilityProvider provider) {
        if (providers == null) {
            addProvider(provider);
            return;
        }
        for (int providerIndex = 0; providerIndex < providers.length; ++providerIndex) {
            ICapabilityProvider storedProvider = providers[providerIndex];
            if (storedProvider.hasCapability(capability, null)) {
                providers[providerIndex] = provider;
                return;
            }
        }
        addProvider(provider);
    }
}
