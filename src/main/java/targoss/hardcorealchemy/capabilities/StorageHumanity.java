package targoss.hardcorealchemy.capabilities;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class StorageHumanity implements Capability.IStorage<ICapabilityHumanity> {

    @Override
    public NBTBase writeNBT(Capability<ICapabilityHumanity> capability, ICapabilityHumanity instance, EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setDouble("humanity", instance.getHumanity());
        nbt.setBoolean("has_humanity_storage", true);
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityHumanity> capability, ICapabilityHumanity instance, EnumFacing side,
            NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        if (!nbtCompound.getBoolean("has_humanity_storage")) {
            instance.setHumanity(instance.getDefaultHumanity());
        }
        else {
            instance.setHumanity(nbtCompound.getDouble("humanity"));
        }
    }
    
}
