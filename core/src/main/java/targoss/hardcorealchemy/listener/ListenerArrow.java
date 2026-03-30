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

package targoss.hardcorealchemy.listener;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityArrow.PickupStatus;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.event.EventArrowUpdate;
import targoss.hardcorealchemy.item.RecipeArrow;
import targoss.hardcorealchemy.util.EntityUtil;

public class ListenerArrow extends HardcoreAlchemyListener {
    @SubscribeEvent
    public void onArrowCheckCrafting(EventArrowUpdate event) {
        EntityArrow arrow = event.arrow;
        if (arrow.world.isRemote) {
            return;
        }
        if (arrow.pickupStatus != PickupStatus.ALLOWED) {
            // Not a player arrow
            return;
        }
        if (arrow.inGround) {
            // Not airborne
            return;
        }
        
        // Check for items in the arrow's way next - assume uncommon
        if (arrowCraftableItemEntity == null) {
            // Initialize predicate
            arrowCraftableItemEntity = Predicates.and(
                    EntityUtil.SELECTOR_IS_ITEM,
                    new Predicate<Entity>() {
                        public boolean apply(@Nullable Entity entity)
                        {
                            EntityItem itemEntity = (EntityItem)entity;
                            ItemStack stack = itemEntity.getEntityItem();
                            if (stack == null) {
                                return false;
                            }
                            Item item = stack.getItem();
                            if (item == null) {
                                return false;
                            }
                            return RecipeArrow.RECIPES.get(item) != null;
                        }
                    }
                );
        }
        List<Entity> nearItemEntities = arrow.world.getEntitiesInAABBexcluding(arrow, arrow.getEntityBoundingBox().addCoord(arrow.motionX, arrow.motionY, arrow.motionZ).expandXyz(1.0), arrowCraftableItemEntity);
        if (nearItemEntities.isEmpty()) {
            return;
        }
        
        // Check arrow trajectory and block collision
        Vec3d start = new Vec3d(arrow.posX, arrow.posY, arrow.posZ);
        Vec3d end = new Vec3d(arrow.posX + arrow.motionX, arrow.posY + arrow.motionY, arrow.posZ + arrow.motionZ);
        // EntityArrow re-constructs the start and end, but there's currently no evidence that rayTraceBlocks modifies start or end
        RayTraceResult blockCollideResult = arrow.world.rayTraceBlocks(start, end, false, true, false);
        if (blockCollideResult != null) {
            end = new Vec3d(blockCollideResult.hitVec.xCoord, blockCollideResult.hitVec.yCoord, blockCollideResult.hitVec.zCoord);
        }
        
        // Compare with item distance
        Tuple<Entity, Double> nearestItemEntityOnPath = findEntityOnPath(arrow, nearItemEntities, start, end);
        if (nearestItemEntityOnPath.getFirst() == null) {
            return;
        }

        // Check if there's an entity in the way of the candidate item, and if so then return
        Tuple<Entity, Double> nearestShootableEntityOnPath = findEntityOnPath(arrow, start, end, EntityUtil.SELECTOR_IS_ARROW_TARGET);
        if (nearestShootableEntityOnPath.getFirst() != null && (nearestShootableEntityOnPath.getSecond() <= nearestItemEntityOnPath.getSecond())) {
            return;
        }
        
        // Use RecipeArrow.RECIPES to create an arrow-crafted output when hit, mark the arrow and input item as dead, and play an arrow hit sound
        arrow.setDead();
        EntityItem input = (EntityItem)nearestItemEntityOnPath.getFirst();
        input.setDead();
        ItemStack stack = input.getEntityItem();
        Item item = stack.getItem();
        RecipeArrow recipe = RecipeArrow.RECIPES.get(item);
        EntityItem output = new EntityItem(arrow.world, input.posX, input.posY, input.posZ, recipe.output.copy());
        double outputMotionX = (arrow.motionX + input.motionX) / 2.0D;
        double outputMotionY = (arrow.motionY + input.motionY) / 2.0D;
        double outputMotionZ = (arrow.motionZ + input.motionZ) / 2.0D;
        double outputSpeed = Math.sqrt((outputMotionX * outputMotionX) + (outputMotionY * outputMotionY) + (outputMotionZ * outputMotionZ));
        // Limit the output's velocity so that it doesn't fly through walls too easily
        final double MAX_ARROW_CRAFT_OUTPUT_SPEED = 2.0D;
        if (outputSpeed > MAX_ARROW_CRAFT_OUTPUT_SPEED) {
            outputMotionX = outputMotionX * MAX_ARROW_CRAFT_OUTPUT_SPEED / outputSpeed;
            outputMotionY = outputMotionY * MAX_ARROW_CRAFT_OUTPUT_SPEED / outputSpeed;
            outputMotionZ = outputMotionZ * MAX_ARROW_CRAFT_OUTPUT_SPEED / outputSpeed;
        }
        output.motionX = outputMotionX;
        output.motionY = outputMotionY;
        output.motionZ = outputMotionZ;
        arrow.world.spawnEntity(output);
        output.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / ((new Random()).nextFloat() * 0.2F + 0.9F));
        
        event.setCanceled(true);
    }
    
    protected Predicate<Entity> arrowCraftableItemEntity;
    
    protected Tuple<Entity, Double> findEntityOnPath(EntityArrow arrow, Vec3d start, Vec3d end, Predicate<Entity> predicate) {
        List<Entity> entities = arrow.world.getEntitiesInAABBexcluding(arrow, arrow.getEntityBoundingBox().addCoord(arrow.motionX, arrow.motionY, arrow.motionZ).expandXyz(1.0), predicate);
        return findEntityOnPath(arrow, entities, start, end);
    }
    
    protected Tuple<Entity, Double> findEntityOnPath(EntityArrow arrow, List<Entity> entities, Vec3d start, Vec3d end) {
        double nearest = 0.0D;
        Entity nearestEntity = null;
        for (Entity entity : entities) {
            if (entity == arrow.shootingEntity && arrow.ticksInAir < 5) {
                continue;
            }
            AxisAlignedBB padded_entity = entity.getEntityBoundingBox().expandXyz(0.3D);
            RayTraceResult intercept = padded_entity.calculateIntercept(start, end);
            if (intercept != null) {
                double distance = start.squareDistanceTo(intercept.hitVec);
                if (distance < nearest || nearest == 0.0D) {
                    nearestEntity = entity;
                    nearest = distance;
                }
            }
        }
        return new Tuple<>(nearestEntity, nearest);
    }
}
