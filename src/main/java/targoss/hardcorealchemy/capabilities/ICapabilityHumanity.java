package targoss.hardcorealchemy.capabilities;

import net.minecraft.entity.ai.attributes.IAttribute;

public interface ICapabilityHumanity {
    public abstract void setHumanity(double humanity);
    public abstract double getHumanity();
    public abstract IAttribute getAttributeMaxHumanity();
    public abstract double getDefaultHumanity();
}
