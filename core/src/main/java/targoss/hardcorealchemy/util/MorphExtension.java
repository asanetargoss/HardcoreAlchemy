package targoss.hardcorealchemy.util;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import targoss.hardcorealchemy.item.Items;

public class MorphExtension {
    public static MorphExtension INSTANCE = new MorphExtension();
    
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
    
    public static class MorphablePredicate implements java.util.function.Predicate<EntityLivingBase>, Predicate<EntityLivingBase> {
        @Override
        public boolean test(EntityLivingBase entity) {
            return INSTANCE.canMorphInto(entity);
        }

        @Override
        public boolean apply(EntityLivingBase entity) {
            return test(entity);
        }
    }
    
    public static class DistanceComparator implements Comparator<Entity> {
        protected double posX;
        protected double posY;
        protected double posZ;
        public DistanceComparator(double posX, double posY, double posZ) {
            this.posX = posX;
            this.posY = posY;
            this.posZ = posZ;
        }
        @Override
        public int compare(Entity entity1, Entity entity2) {
            double distanceSquared1 = entity1.getDistanceSq(posX, posY, posZ);
            double distanceSquared2 = entity2.getDistanceSq(posX, posY, posZ);
            if (distanceSquared1 == distanceSquared2) {
                return 0;
            } else if (distanceSquared1 < distanceSquared2) {
                return -1;
            } else {
                return 1;
            }
        }
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
