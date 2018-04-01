/*
 * Copyright 2017-2018 asanetargoss
 * 
 * This file is part of Hardcore Alchemy.
 * 
 * Hardcore Alchemy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 * 
 * Hardcore Alchemy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hardcore Alchemy.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.listener;

import static targoss.hardcorealchemy.HardcoreAlchemy.LOGGER;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.core.Logger;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITargetNonTamed;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.capability.combatlevel.CapabilityCombatLevel;
import targoss.hardcorealchemy.capability.combatlevel.ICapabilityCombatLevel;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.entity.ai.AIAttackTargetMobOrMorph;
import targoss.hardcorealchemy.entity.ai.AISpiderTargetMobOrMorph;
import targoss.hardcorealchemy.entity.ai.AITargetUnmorphedPlayer;
import targoss.hardcorealchemy.entity.ai.AIUntamedAttackMobOrMorph;
import targoss.hardcorealchemy.util.MobLists;

public class ListenerMobAI extends ConfiguredListener {
    public ListenerMobAI(Configs configs) {
        super(configs);
    }

    @CapabilityInject(ICapabilityCombatLevel.class)
    public static Capability<ICapabilityCombatLevel> COMBAT_LEVEL_CAPABILITY = null;
    public static final ResourceLocation COMBAT_LEVEL_RESOURCE_LOCATION = CapabilityCombatLevel.RESOURCE_LOCATION;
    
    public static Set<String> mobAIIgnoreMorphList = new HashSet();
    
    static {
        MobLists mobLists = new MobLists();
        for (String mob : mobLists.getBosses()) {
            mobAIIgnoreMorphList.add(mob);
        }
        for (String mob : mobLists.getNonMobs()) {
            mobAIIgnoreMorphList.add(mob);
        }
    }
    
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        // Persuade entities that morphs aren't human, unless said entity knows better
        Entity entity = event.getEntity();
        if (entity instanceof EntityLiving && !mobAIIgnoreMorphList.contains(EntityList.getEntityString(entity))) {
            EntityLiving entityLiving = (EntityLiving)entity;
            wrapReplaceAttackAI(entityLiving, EntityAINearestAttackableTarget.class, AIAttackTargetMobOrMorph.class);
            wrapReplaceAttackAI(entityLiving, EntityAITargetNonTamed.class, AIUntamedAttackMobOrMorph.class);
            wrapReplaceAttackAI(entityLiving, EntitySpider.AISpiderTarget.class, AISpiderTargetMobOrMorph.class);
            wrapReplaceAttackAI(entityLiving, EntityAIFindEntityNearestPlayer.class, AITargetUnmorphedPlayer.class);
        }
    }
    
    /**
     * Replace an instance of the AI EntityAINearestAttackableTarget. Assume the
     * replacement AI's constructor takes the old AI instance as a parameter.
     * If it doesn't, you will get errors, because reflection.
     */
    private static void wrapReplaceAttackAI(EntityLiving entityLiving,
            Class<? extends EntityAIBase> targetClazz,
            Class<? extends EntityAIBase> replaceClazz) {
        try {
            Constructor<? extends EntityAIBase> replaceConstructor = replaceClazz.getConstructor(targetClazz);
            
            // Find instances of the AI to replace
            EntityAITasks targetTaskList = entityLiving.targetTasks;
            List<EntityAIBase> aisToReplace = new ArrayList<EntityAIBase>();
            List<Integer> prioritiesToReplace = new ArrayList<Integer>();
            for (EntityAITasks.EntityAITaskEntry targetTask : targetTaskList.taskEntries) {
                if (targetClazz.getName().equals(targetTask.action.getClass().getName())) {
                    aisToReplace.add(targetTask.action);
                    prioritiesToReplace.add(targetTask.priority);
                }
            }
            
            // Replace the AIs with new AIs that take morphs into account, while maintaining the same AI priority
            for (int i=0;i<aisToReplace.size();i++) {
                targetTaskList.removeTask(aisToReplace.get(i));
                targetTaskList.addTask(prioritiesToReplace.get(i),
                            replaceConstructor.newInstance(aisToReplace.get(i))
                        );
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
