package targoss.hardcorealchemy.capability;

import net.minecraft.nbt.NBTBase;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class CapUtil {
    /**
     * Copy pretty much any capability by taking advantage of NBT serialization
     * Returns true if successful
     */
    public static <T,V extends ICapabilityProvider,W extends ICapabilityProvider> boolean
            copyOldToNew(Capability<T> capability, V oldProvider, W newProvider) {
        T oldCapability = oldProvider.getCapability(capability, null);
        if (oldCapability == null) {
            return false;
        }
        T newCapability = newProvider.getCapability(capability, null);
        if (newCapability == null) {
            return false;
        }
        IStorage<T> storage = capability.getStorage();
        NBTBase oldNBT = storage.writeNBT(capability, oldCapability, null);
        storage.readNBT(capability, newCapability, null, oldNBT);
        return true;
    }
}
