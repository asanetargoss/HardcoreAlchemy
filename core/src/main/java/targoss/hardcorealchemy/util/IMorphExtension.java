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

public interface IMorphExtension {
    public class MorphablePredicate implements java.util.function.Predicate<EntityLivingBase>, Predicate<EntityLivingBase> {
        @Override
        public boolean test(EntityLivingBase entity) {
            return MorphExtension.INSTANCE.canMorphInto(entity);
        }
    
        @Override
        public boolean apply(EntityLivingBase entity) {
            return test(entity);
        }
    }
    public class DistanceComparator implements Comparator<Entity> {
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
    public boolean shouldDrawHumanityDottedIcons();
    public boolean canUseHighMagicWithoutBuff(EntityPlayer player);
    public boolean canUseHighMagic(EntityPlayer player);
    public boolean isGhost(EntityLivingBase entity);
    public boolean canMorphInto(EntityLivingBase entity);
    <T extends EntityLivingBase> List<T> getEntitiesAndMorphs(World world, Class<? extends T> entityClass, AxisAlignedBB aabb);
    /**
     * Gets living entities and morphed players who resemble the given entity class,
     * excluding the given entity.
     */
    <T extends EntityLivingBase> List<T> getEntitiesAndMorphsExcluding(EntityLivingBase excludingEntity, World world, Class <? extends T> entityClass, AxisAlignedBB aabb, @Nullable Predicate <? super T > filter);
    <T extends EntityLivingBase> List<T> getEntitiesAndMorphsExcluding(EntityLivingBase excludingEntity, World world, Class <? extends T> entityClass, AxisAlignedBB aabb);
    /**
     * Whether this entity looks like the given entityClass.
     */
    boolean isEntityLike(EntityLivingBase entity, Class<? extends EntityLivingBase> entityClass);
    boolean isUnmorphed(EntityPlayer player);
    boolean isMorphedAs(EntityPlayer player, Class<? extends EntityLivingBase> entityClass);
    EntityLivingBase getEffectiveEntity(@Nonnull EntityPlayer player);
    EntityLivingBase getEffectiveEntity(@Nonnull EntityLivingBase entity);
}
