package targoss.hardcorealchemy.capability.combatlevel;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import targoss.hardcorealchemy.capability.combatlevel.ICapabilityCombatLevel;

public class StorageCombatLevel implements Capability.IStorage<ICapabilityCombatLevel> {

    @Override
    public NBTBase writeNBT(Capability<ICapabilityCombatLevel> capability, ICapabilityCombatLevel instance, EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("value", instance.getValue());
        nbt.setBoolean("hasCombatLevel", instance.getHasCombatLevel());
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityCombatLevel> capability, ICapabilityCombatLevel instance, EnumFacing side,
            NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        instance.setValue(nbtCompound.getInteger("value"));
        instance.setHasCombatLevel(nbtCompound.getBoolean("hasCombatLevel"));
    }

}
