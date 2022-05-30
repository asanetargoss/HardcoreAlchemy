/*
 * Copyright 2017-2022 asanetargoss
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

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import targoss.hardcorealchemy.item.Items;

public class MorphExtension implements IMorphExtension {
    public static IMorphExtension INSTANCE = new MorphExtension();

    @Override
    public boolean shouldDrawHumanityDottedIcons() {
        return false;
    }
    
    public boolean canUseHighMagicWithoutBuff(EntityPlayer player) {
        return true;
    }
    
    public boolean canUseHighMagic(EntityPlayer player) {
        if (canUseHighMagicWithoutBuff(player)) {
            return true;
        }
        return player.getActivePotionEffect(Items.POTION_ALLOW_MAGIC) != null;
    }
    
    public boolean isGhost(EntityLivingBase entity) {
        return false;
    }
    
    public boolean canMorphInto(EntityLivingBase entity) {
        return true;
    }
    
    /**
     * Gets living entities and morphed players who resemble the given entity class
     */
    @SuppressWarnings("unchecked")
    public <T extends EntityLivingBase> List<T> getEntitiesAndMorphs(World world, Class<? extends T> entityClass, AxisAlignedBB aabb, @Nullable Predicate <? super T > filter) {
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
    
    public <T extends EntityLivingBase> List<T> getEntitiesAndMorphs(World world, Class<? extends T> entityClass, AxisAlignedBB aabb) {
        return getEntitiesAndMorphs(world, entityClass, aabb, null);
    }

    /**
     * Gets living entities and morphed players who resemble the given entity class,
     * excluding the given entity.
     */
    public <T extends EntityLivingBase> List<T> getEntitiesAndMorphsExcluding(EntityLivingBase excludingEntity, World world, Class <? extends T> entityClass, AxisAlignedBB aabb, @Nullable Predicate <? super T > filter) {
        List<T> foundEntities = getEntitiesAndMorphs(world, entityClass, aabb, filter);
        foundEntities.remove(excludingEntity);
        return foundEntities;
    }
    
    public <T extends EntityLivingBase> List<T> getEntitiesAndMorphsExcluding(EntityLivingBase excludingEntity, World world, Class <? extends T> entityClass, AxisAlignedBB aabb) {
        return getEntitiesAndMorphsExcluding(excludingEntity, world, entityClass, aabb, null);
    }
    
    /**
     * Whether this entity looks like the given entityClass.
     */
    public boolean isEntityLike(EntityLivingBase entity, Class<? extends EntityLivingBase> entityClass) {
        if (entity instanceof EntityPlayer) {
            return isMorphedAs((EntityPlayer)entity, entityClass);
        }
        else {
            return entityClass.isInstance(entity);
        }
    }

    public boolean isUnmorphed(EntityPlayer player) {
        return true;
    }
    
    public boolean isMorphedAs(EntityPlayer player, Class<? extends EntityLivingBase> entityClass) {
        return entityClass.isInstance(player);
    }
    
    public EntityLivingBase getEffectiveEntity(@Nonnull EntityPlayer player) {
        return player;
    }
    
    public EntityLivingBase getEffectiveEntity(@Nonnull EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            return getEffectiveEntity((EntityPlayer)entity);
        }
        return entity;
    }
}
