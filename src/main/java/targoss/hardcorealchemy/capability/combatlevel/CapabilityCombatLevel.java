package targoss.hardcorealchemy.capability.combatlevel;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.humanity.CapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.ICapabilityHumanity;
import targoss.hardcorealchemy.capability.humanity.StorageHumanity;

public class CapabilityCombatLevel implements ICapabilityCombatLevel {
    
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "combat_level");
    
    private int combatLevel;
    private boolean hasCombatLevel;
    
    public CapabilityCombatLevel() {
        this.combatLevel = 0;
        this.hasCombatLevel = false;
    }
    
    public static void register() {
        CapabilityManager.INSTANCE.register(ICapabilityCombatLevel.class, new StorageCombatLevel(), CapabilityCombatLevel.class);
    }
    
    @Override
    public int getValue() {
        return combatLevel;
    }

    @Override
    public void setValue(int combatLevel) {
        this.combatLevel = Math.max(combatLevel, 0);
    }
    
    public static float getDamageMultiplier(int attackerLevel, int defenderLevel) {
        return Math.max(1.0F + (/*1.0F**/(
                ( ((float)attackerLevel+10.0F) / ((float)defenderLevel+10.0F) ) - 1.0F
                )), 1.0E-2F);
    }
    
    @Override
    public float getGivenDamageMultiplier(int otherCombatLevel) {
        return getDamageMultiplier(combatLevel, otherCombatLevel);
    }

    @Override
    public float getReceivedDamageMultiplier(int otherCombatLevel) {
        return getDamageMultiplier(otherCombatLevel, combatLevel);
    }

    @Override
    public boolean getHasCombatLevel() {
        return hasCombatLevel;
    }

    @Override
    public void setHasCombatLevel(boolean hasCombatLevel) {
        this.hasCombatLevel = hasCombatLevel;
    }

}
