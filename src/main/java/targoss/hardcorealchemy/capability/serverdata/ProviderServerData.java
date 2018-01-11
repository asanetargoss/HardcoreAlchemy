package targoss.hardcorealchemy.capability.serverdata;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ProviderServerData implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(ICapabilityServerData.class)
    public static final Capability<ICapabilityServerData> SERVER_DATA_CAPABILITY = null;
    
    public final ICapabilityServerData instance;
    
    public ProviderServerData() {
        this.instance = SERVER_DATA_CAPABILITY.getDefaultInstance();
    }
    
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == SERVER_DATA_CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == SERVER_DATA_CAPABILITY) {
            return (T)instance;
        }
        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return SERVER_DATA_CAPABILITY.getStorage().writeNBT(SERVER_DATA_CAPABILITY, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        SERVER_DATA_CAPABILITY.getStorage().readNBT(SERVER_DATA_CAPABILITY, instance, null, nbt);
    }

}
