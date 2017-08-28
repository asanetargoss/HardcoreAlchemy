package targoss.hardcorealchemy.capability.humanity;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class StorageHumanity implements Capability.IStorage<ICapabilityHumanity> {

    @Override
    public NBTBase writeNBT(Capability<ICapabilityHumanity> capability, ICapabilityHumanity instance, EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setDouble("humanity", instance.getHumanity());
        nbt.setDouble("lastHumanity", instance.getLastHumanity());
        nbt.setInteger("tick", instance.getTick());
        nbt.setBoolean("hasLostHumanity", instance.getHasLostHumanity());
        nbt.setBoolean("hasLostMorphAbility", instance.getHasLostMorphAbility());
        nbt.setBoolean("isMarried", instance.getIsMarried());
        nbt.setBoolean("isMage", instance.getIsMage());
        nbt.setBoolean("highMagicOverride", instance.getHighMagicOverride());
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityHumanity> capability, ICapabilityHumanity instance, EnumFacing side,
            NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        instance.setHumanity(nbtCompound.getDouble("humanity"));
        instance.setLastHumanity(nbtCompound.getDouble("lastHumanity"));
        instance.setTick(nbtCompound.getInteger("tick"));
        instance.setHasLostHumanity(nbtCompound.getBoolean("hasLostHumanity"));
        instance.setHasLostMorphAbility(nbtCompound.getBoolean("hasLostMorphAbility"));
        instance.setIsMarried(nbtCompound.getBoolean("isMarried"));
        instance.setIsMage(nbtCompound.getBoolean("isMage"));
        instance.setHighMagicOverride(nbtCompound.getBoolean("highMagicOverride"));
    }
    
}
