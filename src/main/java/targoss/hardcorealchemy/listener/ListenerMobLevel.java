package targoss.hardcorealchemy.listener;

import static targoss.hardcorealchemy.HardcoreAlchemy.LOGGER;

import java.util.HashSet;
import java.util.Set;

import mchorse.metamorph.entity.EntityMorph;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.combatlevel.CapabilityCombatLevel;
import targoss.hardcorealchemy.capability.combatlevel.ICapabilityCombatLevel;
import targoss.hardcorealchemy.capability.combatlevel.ProviderCombatLevel;
import targoss.hardcorealchemy.event.EventLivingAttack;
import targoss.hardcorealchemy.util.MobLevelRange;
import targoss.hardcorealchemy.util.MobLists;

public class ListenerMobLevel {
    @CapabilityInject(ICapabilityCombatLevel.class)
    public static Capability<ICapabilityCombatLevel> COMBAT_LEVEL_CAPABILITY = null;
    public static final ResourceLocation COMBAT_LEVEL_RESOURCE_LOCATION = CapabilityCombatLevel.RESOURCE_LOCATION;
    
    public static Set<String> levelBlacklist = new HashSet();
    
    static {
        MobLists mobLists = new MobLists();
        for (String mob : mobLists.getBosses()) {
            levelBlacklist.add(mob);
        }
        for (String mob : mobLists.getNonMobs()) {
            levelBlacklist.add(mob);
        }
    }
    
    @SubscribeEvent
    public void onAttachCapability(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (entity instanceof EntityLivingBase && !(entity instanceof EntityPlayer)) {
            World world = entity.worldObj;
            if (world != null && world.isRemote) {
                return;
            }
            EntityLivingBase entityLiving = (EntityLivingBase)entity;
            if (!levelBlacklist.contains(entityLiving.getClass().getName())) {
                event.addCapability(COMBAT_LEVEL_RESOURCE_LOCATION, new ProviderCombatLevel());
                }
        }
        
    }
    
    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        ICapabilityCombatLevel combatLevel = entity.getCapability(COMBAT_LEVEL_CAPABILITY, null);
        if (combatLevel != null && !combatLevel.getHasCombatLevel()) {
            combatLevel.setHasCombatLevel(true);
            MobLevelRange levelRange = MobLevelRange.getRange(entity.dimension, entity.posY);
            int level = levelRange.getRandomLevel(entity.posX, entity.posZ, entity.worldObj.getSeed());
            combatLevel.setValue(level);
            }
    }
    
    
    @SubscribeEvent
    public void onLivingHurt(EventLivingAttack event) {
        DamageSource source = event.source;
        Entity entity = source.getEntity();
        if (entity == null) {
            // ¯\_(ツ)_/¯
            return;
        }
        World world = entity.worldObj;
        if (world != null && world.isRemote) {
            return;
        }
        if (entity == null || !(entity instanceof EntityLivingBase)) {
            return;
        }
        
        EntityLivingBase attacker = (EntityLivingBase)source.getEntity();
        EntityLivingBase defender = event.entity;
        
        boolean attackerIsPlayer = attacker instanceof EntityPlayer;
        boolean defenderIsPlayer = defender instanceof EntityPlayer;
        if (attackerIsPlayer && defenderIsPlayer) {
            return;
        }
        
        int attackerLevel = 0;
        int defenderLevel = 0;
        
        if (attackerIsPlayer) {
            attackerLevel = ((EntityPlayer)attacker).experienceLevel;
        }
        else if (attacker.hasCapability(COMBAT_LEVEL_CAPABILITY, null)) {
            attackerLevel = attacker.getCapability(COMBAT_LEVEL_CAPABILITY, null).getValue();
        }
        else {
            return;
        }
        if (defenderIsPlayer) {
            defenderLevel = ((EntityPlayer)defender).experienceLevel;
        }
        else if (attacker.hasCapability(COMBAT_LEVEL_CAPABILITY, null)) {
            defenderLevel = defender.getCapability(COMBAT_LEVEL_CAPABILITY, null).getValue();
        }
        else {
            return;
        }
        
        float hurtMultiplier = CapabilityCombatLevel.getDamageMultiplier(attackerLevel, defenderLevel);
        event.amount = event.amount * hurtMultiplier;
        }
}
