package targoss.hardcorealchemy.capability.humanity;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ProviderHumanity implements ICapabilitySerializable<NBTBase> {
    
    @CapabilityInject(ICapabilityHumanity.class)
    public static final Capability<ICapabilityHumanity> HUMANITY_CAPABILITY = null;
    
    public final ICapabilityHumanity instance;
    
    public ProviderHumanity() {
        this.instance = HUMANITY_CAPABILITY.getDefaultInstance();
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == HUMANITY_CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == HUMANITY_CAPABILITY) {
            return (T)instance;
        }
        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return HUMANITY_CAPABILITY.getStorage().writeNBT(HUMANITY_CAPABILITY, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        HUMANITY_CAPABILITY.getStorage().readNBT(HUMANITY_CAPABILITY, instance, null, nbt);
    }

}
