package targoss.hardcorealchemy.tweaks.capability.itemcontainer;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

public class ProviderItemContainer implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(ICapabilityItemContainer.class)
    public static final Capability<ICapabilityItemContainer> CAPABILITY_ITEM_CONTAINER = null;
    
    public final ICapabilityItemContainer instance = CAPABILITY_ITEM_CONTAINER.getDefaultInstance();

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CAPABILITY_ITEM_CONTAINER;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CAPABILITY_ITEM_CONTAINER) {
            return (T)instance;
        }
        return null;
    }

    @Override
    public NBTBase serializeNBT() {
        return CAPABILITY_ITEM_CONTAINER.getStorage().writeNBT(CAPABILITY_ITEM_CONTAINER, instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        CAPABILITY_ITEM_CONTAINER.getStorage().readNBT(CAPABILITY_ITEM_CONTAINER, instance, null, nbt);
    }

}
