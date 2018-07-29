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

package targoss.hardcorealchemy.entity.ai;

import static targoss.hardcorealchemy.HardcoreAlchemy.LOGGER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import mchorse.metamorph.api.morphs.AbstractMorph;
import mchorse.metamorph.capabilities.morphing.IMorphing;
import mchorse.metamorph.capabilities.morphing.MorphingProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.SkeletonType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntitySelectors;
import targoss.hardcorealchemy.util.EntityUtil;

/**
 * Should only be used to replace its superclass.
 */
public class AIAttackTargetMobOrMorph<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T> {
    // This would be heckuva lot easier if it weren't for those stinkin' generics! But I digress...

    public EntityLiving entity;
    public Predicate <? super EntityPlayer> targetPlayerEntitySelector;
    public EntityLivingBase targetEntityNotStrict;
    
    public AIAttackTargetMobOrMorph(EntityAINearestAttackableTarget<T> AIIgnoringMorph, EntityLiving entity) {
        super(AIIgnoringMorph.taskOwner, AIIgnoringMorph.targetClass, AIIgnoringMorph.targetChance,
                AIIgnoringMorph.shouldCheckSight, AIIgnoringMorph.nearbyOnly, null);
        this.targetEntitySelector = AIIgnoringMorph.targetEntitySelector;
        this.targetPlayerEntitySelector = new Predicate<EntityPlayer>()
        {
            public boolean apply(@Nullable EntityPlayer p_apply_1_)
            {
                return p_apply_1_ == null ? false :(!EntitySelectors.NOT_SPECTATING.apply(p_apply_1_) ? false : isSuitableTarget(p_apply_1_, false));
            }
        };
        this.entity = entity;
    }
    
    private boolean shouldTarget(EntityLivingBase candidate) {
        return EntityUtil.isEntityLike(candidate, targetClass) && !EntityUtil.isEntityLike(candidate, entity.getClass());
    }
    
    // Copyright Mojang blah blah
    @Override
    public boolean shouldExecute() {
        if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0)
        {
            return false;
        }
        if (this.targetClass != EntityPlayer.class && this.targetClass != EntityPlayerMP.class)
        {
            List<EntityLivingBase> list = (List<EntityLivingBase>)this.taskOwner.world.<T>getEntitiesWithinAABB(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
            list.addAll(this.taskOwner.world.<EntityPlayer>getEntitiesWithinAABB(EntityPlayer.class, this.getTargetableArea(this.getTargetDistance()), this.targetPlayerEntitySelector));
            
            // Remove entities that do not look like the entity this AI is trying to target
            for (int i = list.size() - 1; i >= 0; i--) {
                if (!shouldTarget(list.get(i))) {
                    list.remove(i);
                }
            }
            if (list.isEmpty())
            {
                return false;
            }
            
            Collections.sort(list, this.theNearestAttackableTargetSorter);
            this.targetEntityNotStrict = list.get(0);
            return true;
        }
        else
        {
            this.targetEntityNotStrict = (T)this.taskOwner.world.getNearestAttackablePlayer(this.taskOwner.posX, this.taskOwner.posY + (double)this.taskOwner.getEyeHeight(), this.taskOwner.posZ, this.getTargetDistance(), this.getTargetDistance(), new Function<EntityPlayer, Double>()
            {
                @Nullable
                public Double apply(@Nullable EntityPlayer p_apply_1_)
                {
                    if (!EntityUtil.isMorphedAs(p_apply_1_, EntityPlayer.class)) {
                        return Double.valueOf(0.0D);
                    }
                    
                    ItemStack itemstack = p_apply_1_.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

                    if (itemstack != null && itemstack.getItem() == Items.SKULL)
                    {
                        int i = itemstack.getItemDamage();
                        boolean flag = taskOwner instanceof EntitySkeleton && ((EntitySkeleton)taskOwner).getSkeletonType() == SkeletonType.NORMAL && i == 0;
                        boolean flag1 = taskOwner instanceof EntityZombie && i == 2;
                        boolean flag2 = taskOwner instanceof EntityCreeper && i == 4;

                        if (flag || flag1 || flag2)
                        {
                            return Double.valueOf(0.5D);
                        }
                    }

                    return Double.valueOf(1.0D);
                }
            }, (Predicate<EntityPlayer>)this.targetEntitySelector);
            return this.targetEntityNotStrict != null;
        }
    }
    
    @Override
    public void startExecuting()
    {
        this.taskOwner.setAttackTarget(this.targetEntityNotStrict);
    }
}
