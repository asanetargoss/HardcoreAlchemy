/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Survival.
 *
 * Hardcore Alchemy Survival is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Survival is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Survival. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.survival.listener;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import targoss.hardcorealchemy.ModState;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.IMorphExtension;
import targoss.hardcorealchemy.util.MorphExtension;

public class ListenerMorphExtension extends HardcoreAlchemyListener {
    public static class Wrapper implements IMorphExtension {
        public IMorphExtension delegate;

        public Wrapper(IMorphExtension delegate) {
            this.delegate = delegate;
        }
        
        @Optional.Method(modid=ModState.TAN_ID)
        public static boolean isTANLowerStartingHealthEnabled() {
            return toughasnails.api.config.SyncedConfig.getBooleanValue(
                    toughasnails.api.config.GameplayOption.ENABLE_LOWERED_STARTING_HEALTH);
        }
        
        @Override
        public boolean shouldDrawHumanityDottedIcons() {
            if (ModState.isTanLoaded && isTANLowerStartingHealthEnabled()) {
                return true;
            }
            return delegate.shouldDrawHumanityDottedIcons();
        }

        @Override
        public boolean canUseHighMagicWithoutBuff(EntityPlayer player) {
            return delegate.canUseHighMagicWithoutBuff(player);
        }

        @Override
        public boolean canUseHighMagic(EntityPlayer player) {
            return delegate.canUseHighMagic(player);
        }

        @Override
        public boolean isGhost(EntityLivingBase entity) {
            return delegate.isGhost(entity);
        }

        @Override
        public boolean canMorphInto(EntityLivingBase entity) {
            return delegate.canMorphInto(entity);
        }

        @Override
        public <T extends EntityLivingBase> List<T> getEntitiesAndMorphs(World world, Class<? extends T> entityClass,
                AxisAlignedBB aabb) {
            return delegate.getEntitiesAndMorphs(world, entityClass, aabb);
        }

        @Override
        public <T extends EntityLivingBase> List<T> getEntitiesAndMorphsExcluding(EntityLivingBase excludingEntity,
                World world, Class<? extends T> entityClass, AxisAlignedBB aabb, Predicate<? super T> filter) {
            return delegate.getEntitiesAndMorphsExcluding(excludingEntity, world, entityClass, aabb, filter);
        }

        @Override
        public <T extends EntityLivingBase> List<T> getEntitiesAndMorphsExcluding(EntityLivingBase excludingEntity,
                World world, Class<? extends T> entityClass, AxisAlignedBB aabb) {
            return delegate.getEntitiesAndMorphsExcluding(excludingEntity, world, entityClass, aabb);
        }

        @Override
        public boolean isEntityLike(EntityLivingBase entity, Class<? extends EntityLivingBase> entityClass) {
            return delegate.isEntityLike(entity, entityClass);
        }

        @Override
        public boolean isUnmorphed(EntityPlayer player) {
            return delegate.isUnmorphed(player);
        }

        @Override
        public boolean isMorphedAs(EntityPlayer player, Class<? extends EntityLivingBase> entityClass) {
            return delegate.isMorphedAs(player, entityClass);
        }

        @Override
        public EntityLivingBase getEffectiveEntity(EntityPlayer player) {
            return delegate.getEffectiveEntity(player);
        }

        @Override
        public EntityLivingBase getEffectiveEntity(EntityLivingBase entity) {
            return delegate.getEffectiveEntity(entity);
        }
        
    }
    
    public void preInit(FMLPreInitializationEvent event) {
        MorphExtension.INSTANCE = new Wrapper(MorphExtension.INSTANCE);
    }
}
