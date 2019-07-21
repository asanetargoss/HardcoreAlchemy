/*
 * Copyright 2018 asanetargoss
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

package targoss.hardcorealchemy.util;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.api.morphs.EntityMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.Morphing;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.ModStateException;
import targoss.hardcorealchemy.coremod.ObfuscatedName;

/**
 * Functions for working with living entities
 */
public class EntityUtil {
    public static Set<Class> humanEntities = new HashSet<>();
    
    static {
        if (!Loader.instance().hasReachedState(LoaderState.AVAILABLE)) {
            throw new ModStateException(
                    "The EntityUtil class should not be used until all mods and their respective entities are registered"
                    );
        }
        
        for (String humanEntityStrings : MobLists.getHumans()) {
            Class clazz = EntityList.NAME_TO_CLASS.get(humanEntityStrings);
            if (clazz != null) {
                humanEntities.add(clazz);
            }
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
    
    public static ITextComponent getEntityName(Class<? extends EntityLivingBase> entityClass) {
        if (customEntityStrings.containsKey(entityClass)) {
            return customEntityStrings.get(entityClass);
        }
        String entityString = EntityList.CLASS_TO_NAME.get(entityClass);
        if (entityString == null) {
            entityString = "generic";
        }
        return new TextComponentTranslation("entity." + entityString + ".name");
    }
    
    public static @Nullable <T extends Entity> T createEntity(Class<T> entityClass) {
        World entityWorld = MiscVanilla.getWorld();
        if (entityWorld == null) {
            HardcoreAlchemy.LOGGER.error("Attempted to create entity of type '" +
                    entityClass.getName() + "', but there is no world to initialize in.");
            return null;
        }
        
        T entity;
        try {
             entity = entityClass.getConstructor(World.class).newInstance(entityWorld);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            HardcoreAlchemy.LOGGER.error("Attempted to create entity of type '" +
                    entityClass.getName() + "', but could not initialize.");
            e.printStackTrace();
            return null;
        }
        
        return entity;
    }
    
    private static final ObfuscatedName INIT_ENTITY_AI = new ObfuscatedName("func_184651_r" /*initEntityAI*/);
    
    private static void forceInitEntityAI(EntityLiving entity) {
        try {
            //Method method = InvokeUtil.getPrivateMethod(true, entity.getClass(), EntityLiving.class, INIT_ENTITY_AI.get());
            Method method = entity.getClass().getDeclaredMethod(INIT_ENTITY_AI.get());
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
    
    private static Map<World, IChunkGenerator> clientSpawnCache = new WeakHashMap<>();
    
    /**
     * Gets the list of entities permitted to spawn at the given location, along with additional spawn information.
     * May be the original or a copy depending on the situation.
     */
    public static List<Biome.SpawnListEntry> getSpawnList(World world, EnumCreatureType creatureType, BlockPos pos) {
        if (!world.isRemote) {
            return ((WorldServer)world).getChunkProvider().getPossibleCreatures(creatureType, pos);
        }
        else {
            if (!clientSpawnCache.containsKey(world)) {
                clientSpawnCache.put(world, world.provider.createChunkGenerator());
            }
            return clientSpawnCache.get(world).getPossibleCreatures(creatureType, pos);
        }
    }
    
    /**
     * Returns true if the entity class has a chance to spawn here, filtering by the given EnumCreatureType.
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
        Set<EntityAITasks.EntityAITaskEntry> aiTargetTasksOriginal = entity.targetTasks.taskEntries;
        if (!entity.world.isRemote) {
            // Normally we would check isRemote, but some mod authors
            // populate AI in the constructor rather than initEntityAI()
            return aiTargetTasksOriginal;
        }
        else {
            Set<EntityAITasks.EntityAITaskEntry> aiTargetTasksCopy = new HashSet<>();
            forceInitEntityAI(entity);
            aiTargetTasksCopy.addAll(aiTargetTasksOriginal);
            aiTargetTasksOriginal.clear();
            return aiTargetTasksCopy;
        }
    }
    
    // Adapted from ItemMonsterPlacer.spawnCreature (1.10.2 stable)
    public static void createLivingEntityAt(EntityLiving entityLiving, float x, float y, float z) {
        entityLiving.setLocationAndAngles(x, y, z, MathHelper.wrapDegrees(entityLiving.world.rand.nextFloat() * 360.0F), 0.0F);
        entityLiving.rotationYawHead = entityLiving.rotationYaw;
        entityLiving.renderYawOffset = entityLiving.rotationYaw;
        entityLiving.onInitialSpawn(entityLiving.world.getDifficultyForLocation(new BlockPos(entityLiving)), null);
        entityLiving.world.spawnEntity(entityLiving);
    }
    
    /**
     * Gets living entities and morphed players who resemble the given entity class
     */
    public static <T extends EntityLivingBase> List<T> getEntitiesAndMorphs(World world, Class<? extends T> entityClass, AxisAlignedBB aabb, @Nullable Predicate <? super T > filter) {
        if (EntityPlayer.class.isAssignableFrom(entityClass)) {
            return (List<T>)world.getEntitiesWithinAABB((Class<? extends T>)entityClass, aabb, new Predicate<T>() {
                @Override public boolean apply(T player) {
                    return isUnmorphed((EntityPlayer)player) && entityClass.isAssignableFrom(player.getClass()) && (filter == null || filter.apply(player));
                }
            });
        }
        else if (entityClass.isAssignableFrom(EntityPlayer.class)) {
            return (List<T>)world.getEntitiesWithinAABB(entityClass, aabb, new Predicate<T>() {
                @Override public boolean apply(T entity) {
                    EntityLivingBase effectiveEntity = getEffectiveEntity(entity);
                    return entityClass.isAssignableFrom(effectiveEntity.getClass()) && (filter == null || filter.apply((T)effectiveEntity));
                }
            });
        }
        else {
            List<T> likeEntities = world.getEntitiesWithinAABB(entityClass, aabb, filter);
            List<EntityPlayer> morphedPlayers = world.getEntitiesWithinAABB(EntityPlayer.class, aabb, new Predicate<EntityPlayer>() {
                @Override public boolean apply(EntityPlayer player) {
                    EntityLivingBase effectiveEntity = getEffectiveEntity(player);
                    return entityClass.isAssignableFrom(effectiveEntity.getClass()) && (filter == null || filter.apply((T)effectiveEntity));
                }
            });
            likeEntities.addAll((List<T>)morphedPlayers);
            return likeEntities;
        }
    }
    
    public static <T extends EntityLivingBase> List<T> getEntitiesAndMorphs(World world, Class<? extends T> entityClass, AxisAlignedBB aabb) {
        return getEntitiesAndMorphs(world, entityClass, aabb, null);
    }

    /**
     * Gets living entities and morphed players who resemble the given entity class,
     * excluding the given entity.
     */
    public static <T extends EntityLivingBase> List<T> getEntitiesAndMorphsExcluding(EntityLivingBase excludingEntity, World world, Class <? extends T> entityClass, AxisAlignedBB aabb, @Nullable Predicate <? super T > filter) {
        List<T> foundEntities = getEntitiesAndMorphs(world, entityClass, aabb, filter);
        foundEntities.remove(excludingEntity);
        return foundEntities;
    }
    
    public static <T extends EntityLivingBase> List<T> getEntitiesAndMorphsExcluding(EntityLivingBase excludingEntity, World world, Class <? extends T> entityClass, AxisAlignedBB aabb) {
        return getEntitiesAndMorphsExcluding(excludingEntity, world, entityClass, aabb, null);
    }
    
    /**
     * Whether this entity looks like the given entityClass.
     */
    public static boolean isEntityLike(EntityLivingBase entity, Class<? extends EntityLivingBase> entityClass) {
        if (entity instanceof EntityPlayer) {
            return isMorphedAs((EntityPlayer)entity, entityClass);
        }
        else {
            return entityClass.isInstance(entity);
        }
    }

    public static boolean isUnmorphed(EntityPlayer player) {
        IMorphing morphing = Morphing.get(player);
        if (morphing == null) {
            return true;
        }
        return morphing.getCurrentMorph() == null;
    }
    
    public static boolean isMorphedAs(EntityPlayer player, Class<? extends EntityLivingBase> entityClass) {
        IMorphing morphing = Morphing.get(player);
        if (morphing == null) {
            return entityClass.isInstance(player);
        }
        AbstractMorph morph = morphing.getCurrentMorph();
        if (morph == null) {
            return entityClass.isInstance(player);
        }
        if (!(morph instanceof EntityMorph)) {
            return false;
        }
        EntityLivingBase morphEntity = ((EntityMorph)morph).getEntity(player.world);
        return entityClass.isInstance(morphEntity);
    }
    
    public static EntityLivingBase getEffectiveEntity(@Nonnull EntityPlayer player) {
        IMorphing morphing = Morphing.get(player);
        if (morphing == null) {
            return player;
        }
        AbstractMorph morph = morphing.getCurrentMorph();
        if (morph == null) {
            return player;
        }
        if (!(morph instanceof EntityMorph)) {
            // Incorrect, but there's no better answer
            return player;
        }
        return ((EntityMorph)morph).getEntity(player.world);
    }
    
    public static EntityLivingBase getEffectiveEntity(@Nonnull EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            return getEffectiveEntity((EntityPlayer)entity);
        }
        return entity;
    }
}
