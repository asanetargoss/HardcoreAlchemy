package targoss.hardcorealchemy.capability.serverdata;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class StorageServerData implements Capability.IStorage<ICapabilityServerData> {
    private static final String HAS_DIFFICULTY = "hasDifficulty";

    @Override
    public NBTBase writeNBT(Capability<ICapabilityServerData> capability, ICapabilityServerData instance, EnumFacing side) {
        NBTTagCompound nbtCompound = new NBTTagCompound();
        
        nbtCompound.setBoolean(HAS_DIFFICULTY, instance.getHasDifficulty());
        
        return nbtCompound;
    }

    @Override
    public void readNBT(Capability<ICapabilityServerData> capability, ICapabilityServerData instance, EnumFacing side,
            NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        
        if (nbtCompound.hasKey(HAS_DIFFICULTY)) {
            instance.setHasDifficulty(nbtCompound.getBoolean(HAS_DIFFICULTY));
        }
    }

}
