/*
 * Copyright 2017-2025 asanetargoss
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

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.util.MobLists;

public class ListenerMobLists extends HardcoreAlchemyListener {
    @SubscribeEvent
    public void onAddMob(MobLists.AddEvent event) {
        switch(event.type) {
        case ENDER_WATER_ALLERGY:
            event.set.add("Enderman");
        // fallthrough
        case WATER_ALLERGY:
            event.set.add("Blaze");
            event.set.add("SnowMan");
            break;
        default:
            break;
        }
    }
}
