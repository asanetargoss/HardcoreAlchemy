/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures.listener;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFindEntityNearestPlayer;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITargetNonTamed;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.capability.entitystate.CapabilityEntityState;
import targoss.hardcorealchemy.capability.entitystate.ICapabilityEntityState;
import targoss.hardcorealchemy.creatures.entity.ai.AIAttackTargetMobOrMorph;
import targoss.hardcorealchemy.creatures.entity.ai.AIPolarBearTargetMobOrMorph;
import targoss.hardcorealchemy.creatures.entity.ai.AISpiderTargetMobOrMorph;
import targoss.hardcorealchemy.creatures.entity.ai.AITargetChosenPlayer;
import targoss.hardcorealchemy.creatures.entity.ai.AITargetUnmorphedPlayer;
import targoss.hardcorealchemy.creatures.entity.ai.AIUntamedAttackMobOrMorph;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.MobLists;

public class ListenerMobAI extends HardcoreAlchemyListener {
    @CapabilityInject(ICapabilityEntityState.class)
    public static Capability<ICapabilityEntityState> ENTITY_STATE_CAPABILITY = null;
    public static final ResourceLocation ENTITY_STATE_RESOURCE_LOCATION = CapabilityEntityState.RESOURCE_LOCATION;
    
    public static Set<String> mobAIIgnoreMorphList = new HashSet<>();
    
    public static class AIReplacer {
        Class<? extends EntityAIBase> targetClazz;
        Class<? extends EntityAIBase> replaceClazz;
        Class<? extends EntityAIBase> delegateClazz;
        public AIReplacer(Class<? extends EntityAIBase> targetClazz, Class<? extends EntityAIBase> replaceClazz, Class<? extends EntityAIBase> delegateClazz) {
            this.targetClazz = targetClazz;
            this.replaceClazz = replaceClazz;
            this.delegateClazz = delegateClazz;
        }
        public AIReplacer(Class<? extends EntityAIBase> targetClazz, Class<? extends EntityAIBase> replaceClazz) {
            this(targetClazz, replaceClazz, targetClazz);
        }
    }
    
    public static Set<AIReplacer> aiReplacers = new HashSet<>();
    
    static {
        mobAIIgnoreMorphList.addAll(MobLists.getBosses());
        mobAIIgnoreMorphList.addAll(MobLists.getNonMobs());
        aiReplacers.add(new AIReplacer(EntityAINearestAttackableTarget.class, AIAttackTargetMobOrMorph.class));
        aiReplacers.add(new AIReplacer(EntityAITargetNonTamed.class, AIUntamedAttackMobOrMorph.class));
        aiReplacers.add(new AIReplacer(EntitySpider.AISpiderTarget.class, AISpiderTargetMobOrMorph.class));
        aiReplacers.add(new AIReplacer(EntityPolarBear.AIAttackPlayer.class, AIPolarBearTargetMobOrMorph.class));
        aiReplacers.add(new AIReplacer(EntityAIFindEntityNearestPlayer.class, AITargetUnmorphedPlayer.class));
    }
    
    public static class DeadlyMonsters {
        public static void loadAITweaks() {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends EntityAIBase> DEADLY_MONSTERS_CLIMBER_AI = (Class<? extends EntityAIBase>)Class.forName("com.dmonsters.entity.EntityClimber$AISpiderTarget");
                aiReplacers.add(new AIReplacer(DEADLY_MONSTERS_CLIMBER_AI, AIAttackTargetMobOrMorph.class, EntityAINearestAttackableTarget.class));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    
    static {
        if (Loader.instance().getIndexedModList().containsKey(ModState.DEADLY_MONSTERS_ID) ) {
            DeadlyMonsters.loadAITweaks();
        }
    }
    
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving)entity;
            
            // Persuade entities that morphs aren't human, unless said entity knows better
            if (!mobAIIgnoreMorphList.contains(EntityList.getEntityString(entity))) {
                for (AIReplacer aiReplacer : aiReplacers) {
                    wrapReplaceAttackAI(entityLiving, aiReplacer);
                }
            }
            
            // Persuade entities to target a specific player if told to do so
            if (entityLiving instanceof EntityCreature) {
                EntityCreature entityCreature = (EntityCreature)entityLiving;
                
                addTargetSpecificPlayerTask(entityCreature);
            }
        }
    }
    
    /**
     * Replace an instance of the AI EntityAIBase. Assume the
     * replacement AI's constructor takes the old AI instance
     * upcasted to delegateClazz as a first parameter, and the
     * AI entity as a second parameter.
     * If it doesn't, you will get errors, because reflection.
     */
    private static void wrapReplaceAttackAI(EntityLiving entityLiving, AIReplacer aiReplacer) {
        try {
            Constructor<? extends EntityAIBase> replaceConstructor = aiReplacer.replaceClazz.getConstructor(aiReplacer.delegateClazz, EntityLiving.class);
            
            // Find instances of the AI to replace
            EntityAITasks targetTaskList = entityLiving.targetTasks;
            List<EntityAIBase> aisToReplace = new ArrayList<EntityAIBase>();
            List<Integer> prioritiesToReplace = new ArrayList<Integer>();
            for (EntityAITasks.EntityAITaskEntry targetTask : targetTaskList.taskEntries) {
                if (aiReplacer.targetClazz.getName().equals(targetTask.action.getClass().getName())) {
                    aisToReplace.add(targetTask.action);
                    prioritiesToReplace.add(targetTask.priority);
                }
            }
            
            // Replace the AIs with new AIs that take morphs into account, while maintaining the same AI priority
            for (int i = 0; i < aisToReplace.size(); i++) {
                targetTaskList.removeTask(aisToReplace.get(i));
                targetTaskList.addTask(prioritiesToReplace.get(i),
                            replaceConstructor.newInstance(aisToReplace.get(i), entityLiving)
                        );
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void addTargetSpecificPlayerTask(EntityCreature entityCreature) {
        int firstPriority = 1;
        EntityAITasks targetTaskList = entityCreature.targetTasks;
        for (EntityAITasks.EntityAITaskEntry targetTask : targetTaskList.taskEntries) {
            firstPriority = Math.min(firstPriority, targetTask.priority - 1);
        }
        targetTaskList.addTask(firstPriority, new AITargetChosenPlayer(entityCreature));
    }
    
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    void onCheckEntityAlive(LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.getHealth() <= 0.0F) {
            return;
        }
        ICapabilityEntityState state = entity.getCapability(ENTITY_STATE_CAPABILITY, null);
        if (state == null) {
            return;
        }
        
        int age = state.getAge() + 1;
        state.setAge(age);
        int lifetime = state.getLifetime();
        if (!entity.world.isRemote && lifetime >= 0 && age >= lifetime) {
            // Entity has reached end of specified lifetime; remove it.
            // Later, we might want to increment the liftime if the entity is chasing a player
            entity.world.removeEntity(entity);
        }
    }
    
}
