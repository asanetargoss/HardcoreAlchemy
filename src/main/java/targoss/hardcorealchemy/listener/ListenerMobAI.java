package targoss.hardcorealchemy.listener;

import static targoss.hardcorealchemy.HardcoreAlchemy.LOGGER;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.core.Logger;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.capability.combatlevel.CapabilityCombatLevel;
import targoss.hardcorealchemy.capability.combatlevel.ICapabilityCombatLevel;
import targoss.hardcorealchemy.entity.ai.AIAttackTargetMobOrMorph;
import targoss.hardcorealchemy.util.MobLists;

public class ListenerMobAI {
    @CapabilityInject(ICapabilityCombatLevel.class)
    public static Capability<ICapabilityCombatLevel> COMBAT_LEVEL_CAPABILITY = null;
    public static final ResourceLocation COMBAT_LEVEL_RESOURCE_LOCATION = CapabilityCombatLevel.RESOURCE_LOCATION;
    
    public static Set<String> mobAIMorphBlacklist = new HashSet();
    public static Set<String> mobAIIgnoreBlacklist = new HashSet();
    
    static {
        MobLists mobLists = new MobLists();
        for (String mob : mobLists.getBosses()) {
            mobAIMorphBlacklist.add(mob);
            mobAIIgnoreBlacklist.add(mob);
        }
        for (String mob : mobLists.getNonMobs()) {
            mobAIMorphBlacklist.add(mob);
            mobAIIgnoreBlacklist.add(mob);
        }
        for (String mob : mobLists.getHumans()) {
            mobAIMorphBlacklist.add(mob);
            mobAIIgnoreBlacklist.add(mob);
        }
    }
    
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        // Persuade entities that morphs aren't human, unless said entity knows better
        Entity entity = event.getEntity();
        if (entity instanceof EntityLiving && !mobAIMorphBlacklist.contains(entity.getClass().getName())) {
            EntityLiving entityLiving = (EntityLiving)entity;
            EntityAITasks targetTaskList = entityLiving.targetTasks;
            // Get all the instances of EntityAINearestAttackableTarget that we wish to override via. replacement
            List<EntityAIBase> aisToReplace = new ArrayList<EntityAIBase>();
            List<Integer> prioritiesToReplace = new ArrayList<Integer>();
            for (EntityAITasks.EntityAITaskEntry targetTask : targetTaskList.taskEntries) {
                // I should technically do class equality, but I think the generics are complicating things
                //TODO: figure out a way to do exact class matching minus the generics part
                if (targetTask.action instanceof EntityAINearestAttackableTarget) {
                    aisToReplace.add(targetTask.action);
                    prioritiesToReplace.add(targetTask.priority);
                }
            }
            // Replace the AIs with new AIs that take morphs into account, while maintaining the same AI priority
            for (int i=0;i<aisToReplace.size();i++) {
                targetTaskList.removeTask(aisToReplace.get(i));
                targetTaskList.addTask(prioritiesToReplace.get(i),
                            new AIAttackTargetMobOrMorph((EntityAINearestAttackableTarget)aisToReplace.get(i))
                        );
            }
        }
    }
}
