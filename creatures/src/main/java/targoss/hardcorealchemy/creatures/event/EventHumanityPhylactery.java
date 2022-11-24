/*
 * Copyright 2017-2022 asanetargoss
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

package targoss.hardcorealchemy.creatures.event;

import java.util.UUID;

import mchorse.metamorph.api.morphs.AbstractMorph;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;
import targoss.hardcorealchemy.capability.misc.ICapabilityMisc;

public class EventHumanityPhylactery extends Event {
    private EventHumanityPhylactery() {}
    
    public static class Create extends EventHumanityPhylactery {
        public final EntityPlayer player;
        public final ICapabilityMisc misc;
        public final AbstractMorph morphTarget;
        public final BlockPos pos;

        public Create(EntityPlayer player, ICapabilityMisc misc, AbstractMorph morphTarget, BlockPos pos) {
            this.player = player;
            this.misc = misc;
            this.morphTarget = morphTarget;
            this.pos = pos;
        }
    }
    
    public static class Destroy extends EventHumanityPhylactery {
        public final World world;
        public final UUID lifetimeUUID;
        public final UUID playerUUID;
        public final BlockPos pos;

        public Destroy(World world, UUID lifetimeUUID, UUID playerUUID, BlockPos pos) {
            this.world = world;
            this.lifetimeUUID = lifetimeUUID;
            this.playerUUID = playerUUID;
            this.pos = pos;
        }
    }
}