package targoss.hardcorealchemy.capability.killcount;

import java.util.Map;

public interface ICapabilityKillCount {
    public abstract int getNumKills(String morphName);
    public abstract void addKill(String morphName);
    public abstract Map<String, Integer> getKillCounts();
    public abstract void setKillCounts(Map<String, Integer> killCounts);
}
