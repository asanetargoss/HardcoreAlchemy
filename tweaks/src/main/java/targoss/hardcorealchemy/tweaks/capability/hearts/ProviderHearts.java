package targoss.hardcorealchemy.tweaks.capability.hearts;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ProviderHearts implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(ICapabilityHearts.class)
    public static final Capability<ICapabilityHearts> HEARTS_CAPABILITY = null;
    
    public final ICapabilityHearts instance = HEARTS_CAPABILITY.getDefaultInstance();

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == HEARTS_CAPABILITY;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == HEARTS_CAPABILITY) {
            return (T)instance;
        }
        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return HEARTS_CAPABILITY.getStorage().writeNBT(HEARTS_CAPABILITY, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        HEARTS_CAPABILITY.getStorage().readNBT(HEARTS_CAPABILITY, instance, null, nbt);
    }

}
