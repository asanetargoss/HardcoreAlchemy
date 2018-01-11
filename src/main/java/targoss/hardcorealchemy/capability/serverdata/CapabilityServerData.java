package targoss.hardcorealchemy.capability.serverdata;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class CapabilityServerData implements ICapabilityServerData {
    
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "serverData");
    
    private boolean hasDifficulty;
    
    public static void register() {
        CapabilityManager.INSTANCE.register(ICapabilityServerData.class, new StorageServerData(), CapabilityServerData.class);
    }

    @Override
    public void setHasDifficulty(boolean hasDifficulty) {
        this.hasDifficulty = hasDifficulty;
    }

    @Override
    public boolean getHasDifficulty() {
        return hasDifficulty;
    }

}
