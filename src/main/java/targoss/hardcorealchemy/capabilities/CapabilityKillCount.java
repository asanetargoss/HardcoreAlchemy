package targoss.hardcorealchemy.capabilities;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class CapabilityKillCount implements ICapabilityKillCount {
    
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "kill_count");
    
    public Map<String, Integer> killCounts;
    
    public CapabilityKillCount() {
        killCounts = new HashMap<String, Integer>();
    }
    
    public static void register() {
        CapabilityManager.INSTANCE.register(ICapabilityKillCount.class, new StorageKillCount(), CapabilityKillCount.class);
    }
    
    @Override
    public int getNumKills(String morphName) {
        if (killCounts.containsKey(morphName)) {
            return killCounts.get(morphName);
        }
        return 0;
    }

    @Override
    public void addKill(String morphName) {
        if (killCounts.containsKey(morphName)) {
            killCounts.put(morphName, killCounts.get(morphName) + 1);
        }
        else {
            killCounts.put(morphName, 1);
        }
    }

    @Override
    public Map<String, Integer> getKillCounts() {
        return killCounts;
    }

    @Override
    public void setKillCounts(Map<String, Integer> killCounts) {
        this.killCounts = killCounts;
    }

}
