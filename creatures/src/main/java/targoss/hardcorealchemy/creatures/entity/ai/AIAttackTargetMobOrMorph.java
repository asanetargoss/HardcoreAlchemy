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

package targoss.hardcorealchemy.creatures.entity.ai;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

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
import targoss.hardcorealchemy.util.MorphExtension;

/**
 * Should only be used to replace its superclass.
 */
public class AIAttackTargetMobOrMorph<T extends EntityLivingBase> extends EntityAINearestAttackableTarget<T> {
    // This would be heckuva lot easier if it weren't for those stinkin' generics! But I digress...

    public EntityLiving entity;
    public Predicate <? super T> targetEntitySelectorNotMorphed;
    public Predicate <? super EntityPlayer> targetPlayerSelector;
    public EntityLivingBase targetEntityNotStrict;

    // NOTE: This code could break if EntityUtil is made to handle entities other than the player being morphed. For now, try to be most efficient.
    public AIAttackTargetMobOrMorph(EntityAINearestAttackableTarget<T> AIIgnoringMorph, EntityLiving entity) {
        super(AIIgnoringMorph.taskOwner, AIIgnoringMorph.targetClass, AIIgnoringMorph.targetChance,
                AIIgnoringMorph.shouldCheckSight, AIIgnoringMorph.nearbyOnly, null);
        this.targetEntitySelector = new Predicate<T>() {
            public boolean apply(@Nullable T candidate) {
                if (candidate instanceof EntityPlayer) {
                    return targetPlayerSelector.apply((EntityPlayer)candidate);
                }
                return targetEntitySelectorNotMorphed.apply(candidate);
            }
        };
        this.targetEntitySelectorNotMorphed = AIIgnoringMorph.targetEntitySelector;
        this.targetPlayerSelector = new Predicate<EntityPlayer>()
        {
            public boolean apply(@Nullable EntityPlayer candidate)
            {
                if (candidate == null) {
                    return false;
                }
                if (!EntitySelectors.NOT_SPECTATING.apply(candidate)) {
                    return false;
                }
                EntityLivingBase effectiveCandidate = MorphExtension.INSTANCE.getEffectiveEntity(candidate);
                if (!targetClass.isInstance(effectiveCandidate)) {
                    return false;
                }
                if (entity.getClass().isInstance(effectiveCandidate)) {
                    // Attacking own kind
                    return false;
                }
                if (!targetEntitySelectorNotMorphed.apply((T)effectiveCandidate)) {
                    return false;
                }
                return isSuitableTarget(candidate, false);
            }
        };
        this.entity = entity;
    }
    
    @Override
    public boolean shouldExecute() {
        if (this.targetChance > 0 && this.taskOwner.getRNG().nextInt(this.targetChance) != 0) {
            return false;
        }
        if (this.targetClass != EntityPlayer.class && this.targetClass != EntityPlayerMP.class) {
            List<EntityLivingBase> list;
            if (this.targetClass.isAssignableFrom(EntityPlayer.class)) {
                list = (List<EntityLivingBase>)this.taskOwner.world.<T>getEntitiesWithinAABB(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
            }
            else {
                list = (List<EntityLivingBase>)this.taskOwner.world.<T>getEntitiesWithinAABB(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelectorNotMorphed);
                list.addAll(this.taskOwner.world.<EntityPlayer>getEntitiesWithinAABB(EntityPlayer.class, this.getTargetableArea(this.getTargetDistance()), this.targetPlayerSelector));
            }
            
            if (list.isEmpty()) {
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
                    if (!MorphExtension.INSTANCE.isMorphedAs(p_apply_1_, EntityPlayer.class)) {
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
            }, (Predicate<EntityPlayer>)this.targetEntitySelectorNotMorphed);
            return this.targetEntityNotStrict != null;
        }
    }
    
    @Override
    public void startExecuting()
    {
        this.taskOwner.setAttackTarget(this.targetEntityNotStrict);
    }
}
