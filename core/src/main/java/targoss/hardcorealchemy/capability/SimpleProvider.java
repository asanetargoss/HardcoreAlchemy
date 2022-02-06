package targoss.hardcorealchemy.capability;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class SimpleProvider<T> implements ICapabilityProvider {
    protected final Capability<T> capability;
    protected final T instance;
    
    public SimpleProvider(Capability<T> capability, T instance) {
        this.capability = capability;
        this.instance = instance;
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == this.capability;
    }

    @SuppressWarnings({ "unchecked", "hiding" })
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == this.capability) {
            return (T)instance;
        }
        return null;
    }

}
