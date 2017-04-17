package targoss.hardcorealchemy.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ProviderKillCount implements ICapabilitySerializable<NBTBase> {
    
    @CapabilityInject(ICapabilityKillCount.class)
    public static final Capability<ICapabilityKillCount> KILL_COUNT_CAPABILITY = null;
    
    public final ICapabilityKillCount instance;
    
    public ProviderKillCount() {
        this.instance = KILL_COUNT_CAPABILITY.getDefaultInstance();
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == KILL_COUNT_CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == KILL_COUNT_CAPABILITY) {
            return (T)instance;
        }
        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return KILL_COUNT_CAPABILITY.getStorage().writeNBT(KILL_COUNT_CAPABILITY, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        KILL_COUNT_CAPABILITY.getStorage().readNBT(KILL_COUNT_CAPABILITY, instance, null, nbt);
    }

}
