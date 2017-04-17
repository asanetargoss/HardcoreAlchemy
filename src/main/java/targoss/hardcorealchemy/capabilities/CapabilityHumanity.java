package targoss.hardcorealchemy.capabilities;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class CapabilityHumanity implements ICapabilityHumanity {
    
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "humanity");
    public static final IAttribute ATTRIBUTE_MAX_HUMANITY = new RangedAttribute(null, HardcoreAlchemy.MOD_ID + ":max_humanity", 4.0D, 1e-45, Double.MAX_VALUE).setShouldWatch(true);
    public static final double DEFAULT_HUMANITY = ATTRIBUTE_MAX_HUMANITY.getDefaultValue();
    
    private double humanity;
    
    public static void register() {
        CapabilityManager.INSTANCE.register(ICapabilityHumanity.class, new StorageHumanity(), CapabilityHumanity.class);
    }
    
    @Override
    public double getDefaultHumanity() {
        return DEFAULT_HUMANITY;
    }
    
    @Override
    public void setHumanity(double humanity) {
        this.humanity = humanity;
    }

    @Override
    public double getHumanity() {
        return this.humanity;
    }

    @Override
    public IAttribute getAttributeMaxHumanity() {
        return ATTRIBUTE_MAX_HUMANITY;
    }

}
