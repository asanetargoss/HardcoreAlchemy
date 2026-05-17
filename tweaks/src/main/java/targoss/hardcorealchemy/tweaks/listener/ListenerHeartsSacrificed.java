/*
 * Copyright 2017-2026 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.listener;

import static targoss.hardcorealchemy.tweaks.item.Items.HEART_TEARS;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.tweaks.capability.hearts.ICapabilityHearts;
import targoss.hardcorealchemy.tweaks.entity.ai.AIEndermanSeenByPlayer;
import targoss.hardcorealchemy.tweaks.item.Items;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.MiscVanilla;

public class ListenerHeartsSacrificed extends HardcoreAlchemyListener {
    @CapabilityInject(ICapabilityHearts.class)
    public static final Capability<ICapabilityHearts> HEARTS_CAPABILITY = null;
    
    @SubscribeEvent
    public void onPlayerTakeFallDamage(LivingFallEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer)entity;
        ICapabilityHearts hearts = player.getCapability(HEARTS_CAPABILITY, null);
        if (hearts == null) {
            return;
        }
        if (!hearts.getSacrificed().contains(HEART_TEARS)) {
            return;
        }
        event.setCanceled(true);
    }
    
    public static Set<EntityUtil.AIReplacer> aiReplacers = new HashSet<>();
    
    static {
        // Makes Endermen ignore the player if they have sacrificed the Heart of Void
        aiReplacers.add(new EntityUtil.AIReplacer(EntityEnderman.AIFindPlayer.class, AIEndermanSeenByPlayer.class));
    }
    
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof EntityLiving) {
            EntityLiving entityLiving = (EntityLiving)entity;
            for (EntityUtil.AIReplacer aiReplacer : aiReplacers) {
                EntityUtil.wrapReplaceAttackAI(entityLiving, aiReplacer);
            }
        }
    }

    protected static final int NEARBY_ENTITY_RANGE = 32;
    protected static final int NEARBY_ENTITY_MAX = 5;
    @SideOnly(Side.CLIENT)
    protected static List<EntityLiving> nearbyVisibleHostileOrAggroEntities = new ArrayList<>(NEARBY_ENTITY_MAX);
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onUpdateFOV(EntityViewRenderEvent.FOVModifier event) {
        EntityUtil.updateViewFrustumNoFar(event);
    }
    
    // Consider using EntityViewRenderEvent.CameraSetup
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTickCheckNearbyEntities(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        nearbyVisibleHostileOrAggroEntities.clear();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.thirdPersonView > 0) {
            // Not currently supported
            return;
        }
        EntityPlayer player = MiscVanilla.getTheMinecraftPlayer();
        if (player == null) {
            return;
        }
        if (player.world == null) {
            return;
        }
        if (!player.world.isRemote) {
            return;
        }
        if (!player.isSneaking()) {
            return;
        }
        ICapabilityHearts hearts = player.getCapability(HEARTS_CAPABILITY, null);
        if (hearts == null) {
            return;
        }
        if (!hearts.getSacrificed().contains(Items.HEART_HUNTER)) {
            return;
        }
        AxisAlignedBB aabb = player.getEntityBoundingBox().expandXyz(NEARBY_ENTITY_RANGE);
        final Entity viewer = mc.getRenderViewEntity();
        // Offset derived from EntityRender.orientCamera (1.10.2)
        float eyeHeightOffset = viewer.getEyeHeight();
        if (viewer instanceof EntityLivingBase && ((EntityLivingBase)viewer).isPlayerSleeping()) {
            eyeHeightOffset += 1.0F;
        }
        final float viewOffsetY = eyeHeightOffset;
        List<Entity> hostileOrAggroEntities = player.world.getEntitiesInAABBexcluding(player, aabb, new Predicate<Entity>() {
            @Override
            public boolean apply(Entity entity) {
                if (!(entity instanceof EntityLiving)) {
                    return false;
                }
                double d = entity.getDistanceToEntity(viewer);
                if (d > NEARBY_ENTITY_RANGE) {
                    return false;
                }
                if (!EntityUtil.isEntityInFrustumStrictIgnoreFarClip(viewer.posX, viewer.posY + viewOffsetY, viewer.posZ, entity)) {
                    return false;
                }
                EntityLiving entityLiving = (EntityLiving)entity;
                if (entityLiving.attackTarget == player) {
                    return true;
                }
                return EntityUtil.isHostileMob(entityLiving);
            }
        });
        hostileOrAggroEntities.sort(new Comparator<Entity>() {
            @Override
            public int compare(Entity e1, Entity e2) {
                double d1 = e1.getDistanceSqToEntity(viewer);
                double d2 = e2.getDistanceSqToEntity(viewer);
                if (d1 == d2) {
                    return 0;
                } else if (d1 < d2) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        int n = Math.min(hostileOrAggroEntities.size(), NEARBY_ENTITY_MAX);
        for (int i = 0; i < n; ++i) {
            nearbyVisibleHostileOrAggroEntities.add((EntityLiving)hostileOrAggroEntities.get(i));
        }
    }
    
    public static boolean hasForceGlow(Entity entity) {
        // Setting the server glow flag can cause a desync, so only set to true on client side
        if (!entity.world.isRemote) {
            return false;
        }
        for (EntityLiving nearbyHostileOrAggro : nearbyVisibleHostileOrAggroEntities) {
            if (entity.equals(nearbyHostileOrAggro)) {
                return true;
            }
        }
        return false;
    }
}
