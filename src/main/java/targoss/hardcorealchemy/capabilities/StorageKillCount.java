package targoss.hardcorealchemy.capabilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

public class StorageKillCount implements Capability.IStorage<ICapabilityKillCount> {

    @Override
    public NBTBase writeNBT(Capability<ICapabilityKillCount> capability, ICapabilityKillCount instance, EnumFacing side) {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagCompound nbtKills = new NBTTagCompound();
        Map<String, Integer> killCounts = instance.getKillCounts();
        for (Entry<String, Integer> killCount : killCounts.entrySet()) {
            String stringKey = killCount.getKey();
            if (stringKey.equals("")) {
                // Empty string not allowed!
                continue;
            }
            nbtKills.setInteger(stringKey, killCount.getValue());
        }
        nbt.setTag("kill_counts", nbtKills);
        return nbt;
    }

    @Override
    public void readNBT(Capability<ICapabilityKillCount> capability, ICapabilityKillCount instance, EnumFacing side, NBTBase nbt) {
        Map<String, Integer> killCounts = new HashMap<String, Integer>();
        // Hurray for pointers!
        instance.setKillCounts(killCounts);
        if (nbt.hasNoTags() || !(nbt instanceof NBTTagCompound)) {
            return;
        }
        NBTTagCompound nbtCompound = (NBTTagCompound)nbt;
        NBTTagCompound nbtKills = nbtCompound.getCompoundTag("kill_counts");
        for (String key : nbtKills.getKeySet()) {
            if (key.equals("")) {
                // Empty string not allowed!
                continue;
            }
            killCounts.put(key, nbtKills.getInteger(key));
        }
    }

}
