package targoss.hardcorealchemy.capability.food;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.util.MorphDiet;
import targoss.hardcorealchemy.util.MorphDiet.Restriction;

public class CapabilityFood implements ICapabilityFood {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "food");
    
    @CapabilityInject(ICapabilityFood.class)
    public static final Capability<ICapabilityFood> FOOD_CAPABILITY = null;
    
    private MorphDiet.Restriction restriction;
    
    public static void register() {
        CapabilityManager.INSTANCE.register(ICapabilityFood.class, new StorageFood(), CapabilityFood.class);
        CapUtil.registerVirtualCapability(RESOURCE_LOCATION, FOOD_CAPABILITY);
    }
    
    public CapabilityFood() {
        this.restriction = null;
    }
    
    @Override
    public Restriction getRestriction() {
        return this.restriction;
    }

    @Override
    public void setRestriction(Restriction restriction) {
        this.restriction = restriction;
    }

}
