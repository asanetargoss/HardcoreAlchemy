/*
 * Copyright 2020 asanetargoss
 * 
 * This file is part of the Hardcore Alchemy capstone mod.
 * 
 * The Hardcore Alchemy capstone mod is free software: you can redistribute
 * it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3 of the
 * License.
 * 
 * The Hardcore Alchemy capstone mod is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the Hardcore Alchemy capstone mod. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.event;

import net.minecraft.block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import targoss.hardcorealchemy.coremod.CoremodHook;

public class EventPlayerDamageBlockSound extends Event {
    public final EntityPlayer player;
    public SoundType soundType;
    public final BlockPos blockPos;

    public EventPlayerDamageBlockSound(EntityPlayer player, SoundType soundType, BlockPos blockPos) {
        this.player = player;
        this.soundType = soundType;
        this.blockPos = blockPos;
    }
    
    @Override
    public boolean isCancelable() {
        return true;
    }
    
    @CoremodHook
    public static SoundType onPlayerDamageBlockSound(SoundType soundType, EntityPlayer player, BlockPos blockPos) {
        EventPlayerDamageBlockSound event = new EventPlayerDamageBlockSound(player, soundType, blockPos);
        boolean canceled = MinecraftForge.EVENT_BUS.post(event);
        return canceled ? null : event.soundType;
    }
}
