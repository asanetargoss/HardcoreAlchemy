package targoss.hardcorealchemy.capability.food;

import targoss.hardcorealchemy.util.MorphDiet;

public interface ICapabilityFood {
    MorphDiet.Restriction getRestriction();
    void setRestriction(MorphDiet.Restriction restriction);
}
