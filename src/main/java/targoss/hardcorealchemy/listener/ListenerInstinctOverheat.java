/*
 * Copyright 2020 asanetargoss
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

package targoss.hardcorealchemy.listener;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.capability.instinct.ICapabilityInstinct;
import targoss.hardcorealchemy.config.Configs;
import targoss.hardcorealchemy.event.EventExtinguishFire;

public class ListenerInstinctOverheat extends ConfiguredListener {
    public ListenerInstinctOverheat(Configs configs) {
        super(configs);
    }
    
    @CapabilityInject(ICapabilityInstinct.class)
    public static final Capability<ICapabilityInstinct> INSTINCT_CAPABILITY = null;
    
    public static boolean isOverheating(EntityPlayer player) {
        targoss.hardcorealchemy.HardcoreAlchemy.LOGGER.error("Overheating!");return true;//TODO: Revert after testing
        /*ICapabilityInstinct instinct = player.getCapability(INSTINCT_CAPABILITY, null);
        if (instinct == null) {
            return false;
        }
        IInstinctEffectData data = instinct.getInstinctEffectData(Instincts.EFFECT_OVERHEAT);
        if (!(data instanceof InstinctEffectOverheat.Data)) {
            return false;
        }
        InstinctEffectOverheat.Data overheatData = (InstinctEffectOverheat.Data)data;
        return overheatData.isOverheating();*/
    }

    @SubscribeEvent
    void onPunchBlockAflame(LeftClickBlock event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.world.isRemote) {
            return;
        }
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
        // TODO: It turns out event.player is always null. ;_; so we have to remove the ALOAD from the coremod hook and use LeftClickBlock to figure out the actual player
        if (event.player == null) {
            return;
        }
        if (!isOverheating(event.player)) {
            return;
        }
        event.setCanceled(true);
    }

}
