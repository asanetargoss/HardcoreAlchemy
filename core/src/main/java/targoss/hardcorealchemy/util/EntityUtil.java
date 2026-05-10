/*
 * Copyright 2017-2026 asanetargoss
 *
 * This file is part of Hardcore Alchemy Core.
 *
 * Hardcore Alchemy Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Core is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Core. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.util;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import targoss.hardcorealchemy.HardcoreAlchemyCore;
import targoss.hardcorealchemy.ModStateException;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

/**
 * Functions for working with living entities
 */
public class EntityUtil {
    static {
        if (!Loader.instance().hasReachedState(LoaderState.AVAILABLE)) {
            throw new ModStateException(
                    "The EntityUtil class should not be used until all mods and their respective entities are registered"
                    );
        }
    }
    
    private static Map<Class<? extends EntityLivingBase>, ITextComponent> customEntityStrings = new HashMap<>();
    static {
        customEntityStrings.put(EntityLivingBase.class, new TextComponentTranslation("entity.Mob.name"));
        // These three are added in the Hardcore Alchemy lang file
        customEntityStrings.put(EntityCreature.class, new TextComponentTranslation("entity.Creature.name"));
        customEntityStrings.put(EntityAnimal.class, new TextComponentTranslation("entity.Animal.name"));
        customEntityStrings.put(EntityPlayer.class, new TextComponentTranslation("entity.Player.name"));
    }
    
    public static ITextComponent getEntityName(EntityLivingBase entity) {
        return getEntityName(entity.getClass());
    }
    
    public static boolean isValidEntityName(String entityString) {
        // If you don't check the string is valid, vanilla could return a pig instead as the entity ID, which we don't want
        if (entityString == null || entityString.isEmpty()) {
            return false;
        }
        return EntityList.NAME_TO_CLASS.containsKey(entityString);
    }
    
    @SuppressWarnings("unchecked")
    public static @Nullable Class<? extends EntityLivingBase> getLivingEntityClassFromString(String entityString) {
        if (!isValidEntityName(entityString)) {
            return null;
        }
        Class<? extends Entity> entityClass = EntityList.NAME_TO_CLASS.get(entityString);
        return (Class<? extends EntityLivingBase>)entityClass;
    }
    
    public static ITextComponent getEntityName(Class<? extends Entity> entityClass) {
        if (customEntityStrings.containsKey(entityClass)) {
            return customEntityStrings.get(entityClass);
        }
        String entityString = EntityList.CLASS_TO_NAME.get(entityClass);
        if (entityString == null) {
            entityString = "generic";
        }
        return new TextComponentTranslation("entity." + entityString + ".name");
    }
    
    public static final Predicate<Entity> SELECTOR_IS_ITEM = new Predicate<Entity>() {
        @Override
        public boolean apply(Entity entity) {
            return (entity instanceof EntityItem) && entity.isEntityAlive();
        }
    };
    
    @SuppressWarnings("unchecked")
    public static final Predicate<Entity> SELECTOR_IS_ARROW_TARGET = Predicates.and(
        EntitySelectors.NOT_SPECTATING,
        EntitySelectors.IS_ALIVE,
        new Predicate<Entity>() {
            public boolean apply(@Nullable Entity entity)
            {
                return entity.canBeCollidedWith();
            }
        },
        new Predicate<Entity>() {
            @Override
            public boolean apply(Entity entity) {
                return (entity instanceof EntityItem) && entity.isEntityAlive();
            }
        }
    );
    
    public static @Nullable <T extends Entity> T createEntity(Class<T> entityClass) {
        World entityWorld = MiscVanilla.getWorld();
        if (entityWorld == null) {
            HardcoreAlchemyCore.LOGGER.error("Attempted to create entity of type '" +
                    entityClass.getName() + "', but there is no world to initialize in.");
            return null;
        }
        
        T entity;
        try {
             entity = entityClass.getConstructor(World.class).newInstance(entityWorld);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            HardcoreAlchemyCore.LOGGER.error("Attempted to create entity of type '" +
                    entityClass.getName() + "', but could not initialize.");
            e.printStackTrace();
            return null;
        }
        
        return entity;
    }
    
    public static class AIReplacer {
        public Class<? extends EntityAIBase> targetClazz;
        public Class<? extends EntityAIBase> replaceClazz;
        public Class<? extends EntityAIBase> delegateClazz;
        public AIReplacer(Class<? extends EntityAIBase> targetClazz, Class<? extends EntityAIBase> replaceClazz, Class<? extends EntityAIBase> delegateClazz) {
            this.targetClazz = targetClazz;
            this.replaceClazz = replaceClazz;
            this.delegateClazz = delegateClazz;
        }
        public AIReplacer(Class<? extends EntityAIBase> targetClazz, Class<? extends EntityAIBase> replaceClazz) {
            this(targetClazz, replaceClazz, targetClazz);
        }
    }

    private static final ObfuscatedName INIT_ENTITY_AI = new ObfuscatedName("func_184651_r" /*initEntityAI*/);
    
    private static void forceInitEntityAI(EntityLiving entity) {
        try {
            Method method = InvokeUtil.getPrivateMethod(true, entity.getClass(), EntityLiving.class, INIT_ENTITY_AI.get());
            method.setAccessible(true);
            method.invoke(entity);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Gets what the entity's AI tasks would be if you were on server-side.
     * May be the original or a copy depending on the situation.
     * Uses reflection.
     */
    public static Set<EntityAITasks.EntityAITaskEntry> getAiTasks(EntityLiving entity) {
        Set<EntityAITasks.EntityAITaskEntry> aiTasksOriginal = entity.tasks.taskEntries;
        if (!aiTasksOriginal.isEmpty()) {
            // Normally we would check isRemote, but some mod authors
            // populate AI in the constructor rather than initEntityAI()
            return aiTasksOriginal;
        }
        else {
            Set<EntityAITasks.EntityAITaskEntry> aiTasksCopy = new HashSet<>();
            forceInitEntityAI(entity);
            aiTasksCopy.addAll(aiTasksOriginal);
            aiTasksOriginal.clear();
            return aiTasksCopy;
        }
    }
    
    /**
     * Replace an instance of the AI EntityAIBase. Assume the
     * replacement AI's constructor takes the old AI instance
     * upcasted to delegateClazz as a first parameter, and the
     * AI entity as a second parameter.
     * If it doesn't, you will get errors, because reflection.
     */
    public static void wrapReplaceAttackAI(EntityLiving entityLiving, AIReplacer aiReplacer) {
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

    /**
     * Gets the list of entities permitted to spawn at the given location, along with additional spawn information.
     * May be the original or a copy depending on the situation.
     * 
     * SERVER-SIDE ONLY
     */
    public static List<Biome.SpawnListEntry> getSpawnList(World world, EnumCreatureType creatureType, BlockPos pos) {
        assert(!world.isRemote);
        if (world.isRemote) {
            return new ArrayList<>();
        }
        return ((WorldServer)world).getChunkProvider().getPossibleCreatures(creatureType, pos);
    }
    
    /**
     * Returns true if the entity class has a chance to spawn here, filtering by the given EnumCreatureType.
     * 
     * SERVER-SIDE ONLY
     */
    public static boolean canEntityClassSpawnHere(Class <? extends EntityLiving> entityClass, World world, BlockPos pos, EnumCreatureType creatureType) {
        if (!creatureType.getCreatureClass().isAssignableFrom(entityClass)) {
            return false;
        }
        
        List<Biome.SpawnListEntry> creatureSpawns = getSpawnList(world, creatureType, pos);
        for (Biome.SpawnListEntry creatureSpawn : creatureSpawns) {
            if (creatureSpawn.entityClass == entityClass) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns true if the entity class has a chance to spawn here.
     * 
     * SERVER-SIDE ONLY
     */
    public static boolean canEntityClassSpawnHere(Class<? extends EntityLiving> entityClass, World world, BlockPos pos) {
        for (EnumCreatureType creatureType : EnumCreatureType.values()) {
            if (canEntityClassSpawnHere(entityClass, world, pos, creatureType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns true if the entity has a chance to spawn here.
     * 
     * SERVER-SIDE ONLY
     */
    public static boolean canEntitySpawnHere(EntityLiving entity, World world, BlockPos pos) {
        return canEntityClassSpawnHere(entity.getClass(), world, pos);
    }
    
    /**
     * Gets what the entity's AI target tasks would be if you were on server-side.
     * May be the original or a copy depending on the situation.
     * Uses reflection.
     */
    public static Set<EntityAITasks.EntityAITaskEntry> getAiTargetTasks(EntityLiving entity) {
        if (!entity.world.isRemote) {
            return entity.targetTasks.taskEntries;
        }
        else {
            // Some mod authors populate AI in the constructor
            // rather than initEntityAI(), so preserve those AI tasks for next time.
            Set<EntityAITasks.EntityAITaskEntry> aiTargetTasksOriginal = entity.targetTasks.taskEntries;
            Set<EntityAITasks.EntityAITaskEntry> aiTargetTasksCopy = new HashSet<>();
            aiTargetTasksCopy.addAll(aiTargetTasksOriginal);
            Set<EntityAITasks.EntityAITaskEntry> aiTargetTasksAll = new HashSet<>();
            forceInitEntityAI(entity);
            aiTargetTasksAll.addAll(aiTargetTasksOriginal);
            aiTargetTasksOriginal.clear();
            aiTargetTasksOriginal.addAll(aiTargetTasksCopy);
            return aiTargetTasksAll;
        }
    }
    
    /**
     * A hostile mob is a mob that attacks players, generally without provocation.
     * This function tries to detect if a mob is hostile based on its AI.
     */
    public static boolean isHostileMob(EntityLiving entity) {
        Set<EntityAITasks.EntityAITaskEntry> aiTargetTasks = getAiTargetTasks(entity);
        for (EntityAITasks.EntityAITaskEntry entry : aiTargetTasks) {
            if (entry.action instanceof EntityAINearestAttackableTarget) {
                @SuppressWarnings("rawtypes")
                Class<?> targetClass = ((EntityAINearestAttackableTarget<?>)entry.action).targetClass;
                if (EntityPlayer.class.isAssignableFrom(targetClass)) {
                    return true;
                }
            }
            if (entry.action.getClass().getSimpleName().toLowerCase().contains("player")) {
                // Probably a player-targeting class
                return true;
            }
        }

        return false;
    }
    
    // Adapted from ItemMonsterPlacer.spawnCreature (1.10.2 stable)
    public static void createLivingEntityAt(EntityLiving entityLiving, float x, float y, float z) {
        entityLiving.setLocationAndAngles(x, y, z, MathHelper.wrapDegrees(entityLiving.world.rand.nextFloat() * 360.0F), 0.0F);
        entityLiving.rotationYawHead = entityLiving.rotationYaw;
        entityLiving.renderYawOffset = entityLiving.rotationYaw;
        entityLiving.onInitialSpawn(entityLiving.world.getDifficultyForLocation(new BlockPos(entityLiving)), null);
        entityLiving.world.spawnEntity(entityLiving);
    }
}
