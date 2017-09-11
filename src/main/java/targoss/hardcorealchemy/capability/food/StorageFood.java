package targoss.hardcorealchemy.capability.food;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import targoss.hardcorealchemy.util.MorphDiet;

public class StorageFood implements Capability.IStorage<ICapabilityFood> {

    @Override
    public NBTBase writeNBT(Capability<ICapabilityFood> capability, ICapabilityFood instance, EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        
        MorphDiet.Restriction restriction = instance.getRestriction();
        String restrictionString = "";
        if (restriction != null) {
            restrictionString = restriction.toString();
        }
        nbt.setString("restriction", restrictionString);
        
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityFood> capability, ICapabilityFood instance, EnumFacing side,
            NBTBase nbt) {
        if (!(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        
        instance.setRestriction(MorphDiet.Restriction.fromString(nbtCompound.getString("restriction")));
    }

}
