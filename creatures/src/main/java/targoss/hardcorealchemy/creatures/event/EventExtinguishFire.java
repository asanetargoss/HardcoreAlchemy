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

package targoss.hardcorealchemy.creatures.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import targoss.hardcorealchemy.coremod.CoremodHook;

public class EventExtinguishFire extends Event {
    public static EntityPlayer currentServerPlayer = null;
    public final EntityPlayer player;

    public EventExtinguishFire() {
        // This works because (hopefully) there is only one Minecraft server thread
        this.player = currentServerPlayer;
    }

    @Override
    public boolean isCancelable() {
        return true;
    }

    @CoremodHook
    public static boolean onExtinguishFire() {
        EventExtinguishFire event = new EventExtinguishFire();
        return !MinecraftForge.EVENT_BUS.post(event);
    }
}
