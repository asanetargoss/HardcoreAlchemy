/*
 * Copyright 2017-2025 asanetargoss
 *
 * This file is part of Hardcore Alchemy LibA3.
 *
 * Hardcore Alchemy LibA3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * Hardcore Alchemy LibA3 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with
 * Hardcore Alchemy LibA3.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.liba3.listener;

import java.util.Set;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.MobLists;

public class ListenerMobLists extends HardcoreAlchemyListener {
    @SubscribeEvent
    public void onAddMob(MobLists.AddEvent event) {
        switch(event.type) {
        case NON_MOB:
            Set<String> nonMobs = event.set;
            
            // Jon's Useless Mod
            nonMobs.add("jum.Useless Arrow");
            
            break;
        case TROLL:
            Set<String> trollMobs = event.set;
            
            // Jon's Useless Mod
            trollMobs.add("jum.Dave the Useless");
            
            break;
        default:
            break;
        
        }
    }
}
