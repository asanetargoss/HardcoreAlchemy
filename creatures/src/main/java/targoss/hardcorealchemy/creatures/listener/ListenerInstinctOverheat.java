/*
 * Copyright 2017-2023 asanetargoss
 *
 * This file is part of Hardcore Alchemy Creatures.
 *
 * Hardcore Alchemy Creatures is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Creatures is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Creatures. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.creatures.listener;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.creatures.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.creatures.event.EventExtinguishFire;
import targoss.hardcorealchemy.creatures.instinct.InstinctEffectOverheat;
import targoss.hardcorealchemy.creatures.instinct.Instincts;
import targoss.hardcorealchemy.creatures.instinct.api.IInstinctEffectData;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;

public class ListenerInstinctOverheat extends HardcoreAlchemyListener {
    @CapabilityInject(ICapabilityInstinct.class)
    public static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;
    
    public static boolean isOverheating(EntityPlayer player) {
        ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return false;
        }
        IInstinctEffectData data = instinct.getInstinctEffectData(Instincts.EFFECT_OVERHEAT);
        if (!(data instanceof InstinctEffectOverheat.Data)) {
            return false;
        }
        InstinctEffectOverheat.Data overheatData = (InstinctEffectOverheat.Data)data;
        return overheatData.isOverheating();
    }

    @SubscribeEvent
    void onPunchBlockAflame(LeftClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) {
            return;
        }
        EventExtinguishFire.currentServerPlayer = player;
        if (!isOverheating(player)) {
            return;
        }
        
        BlockPos aflamePos = event.getPos().offset(event.getFace(), 1);
        if (!player.world.isAirBlock(aflamePos)) {
            return;
        }
        player.world.setBlockState(aflamePos, Blocks.FIRE.getDefaultState());
    }

    /** Under overheat effect, punching fire does not put it out! */
    @SubscribeEvent
    void onPutOutFire(EventExtinguishFire event) {
        if (event.player == null) {
            return;
        }
        if (!isOverheating(event.player)) {
            return;
        }
        event.setCanceled(true);
    }

}
