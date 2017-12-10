package targoss.hardcorealchemy.coremod;

public class ObfuscatedName {
    String mcpName;
    String srgName;
    
    public ObfuscatedName(String mcpName, String srgName) {
        this.mcpName = mcpName;
        this.srgName = srgName;
    }
    
    public String get() {
        return HardcoreAlchemyCoreMod.obfuscated ? srgName : mcpName;
    }
}
