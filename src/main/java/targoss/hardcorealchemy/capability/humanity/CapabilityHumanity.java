package targoss.hardcorealchemy.capability.humanity;

import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class CapabilityHumanity implements ICapabilityHumanity {
    
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "humanity");
    
    private double humanity;
    // Humanity in the previous tick after humanity tick calculations; allows us to see if humanity was changed in other ways
    private double lastHumanity;
    // Time passed since humanity data last passed to client
    private int tick;
    private boolean hasLostHumanity;
    private boolean hasLostMorphAbility;
    private boolean isMarried;
    private boolean isMage;
    
    public static void register() {
        CapabilityManager.INSTANCE.register(ICapabilityHumanity.class, new StorageHumanity(), CapabilityHumanity.class);
    }
    
    public CapabilityHumanity() {
        humanity = DEFAULT_HUMANITY_VALUE;
        lastHumanity = DEFAULT_HUMANITY_VALUE;
        tick = 0;
        hasLostHumanity = false;
        hasLostMorphAbility = false;
        isMarried = false;
        isMage = false;
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
    public void setTick(int tick) {
        this.tick = tick;
    }
    
    @Override
    public int getTick() {
        return tick;
    }
    
    @Override
    public void setHasLostHumanity(boolean hasLostHumanity) {
        this.hasLostHumanity = hasLostHumanity;
    }
    
    @Override
    public void setIsMarried(boolean isMarried) {
        this.isMarried = isMarried;
    }
    
    @Override
    public void setIsMage(boolean isMage) {
        this.isMage = isMage;
    }
    
    @Override
    public boolean getHasLostHumanity() {
        return hasLostHumanity;
    }
    
    @Override
    public boolean getIsMarried() {
        return isMarried;
    }
    
    @Override
    public boolean getIsMage() {
        return isMage;
    }
    
    @Override
    public boolean canMorph() {
        return !(hasLostHumanity || hasLostMorphAbility || isMarried || isMage);
    }
    
    @Override
    public boolean shouldDisplayHumanity() {
        return humanity > 0 && !(hasLostHumanity || hasLostMorphAbility || isMarried || isMage);
    }
    
    @Override
    public String explainWhyCantMorph() {
        if (hasLostMorphAbility) {
            return "You fail to comprehend other forms.";
        }
        if (hasLostHumanity) {
            return "Your humanity is gone. You no longer have magic.";
        }
        if (isMarried) {
            return "Your humanity is strengthened by the power of love. You cannot find it in you to become a beast.";
        }
        if (isMage) {
            return "Your affinity to hermitian magic renders your soul incompatible with anything other than a human body.";
        }
        return "";
    }

    @Override
    public void setHasLostMorphAbility(boolean hasLostMorphAbility) {
        this.hasLostMorphAbility = hasLostMorphAbility;
    }

    @Override
    public boolean getHasLostMorphAbility() {
        return hasLostMorphAbility;
    }

    @Override
    public void setLastHumanity(double lastHumanity) {
        this.lastHumanity = lastHumanity;
    }

    @Override
    public double getLastHumanity() {
        return lastHumanity;
    }

}
