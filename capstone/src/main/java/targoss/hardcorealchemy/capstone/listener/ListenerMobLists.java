/*
 * Copyright 2017-2025 asanetargoss
 *
 * This file is part of Hardcore Alchemy Capstone.
 *
 * Hardcore Alchemy Capstone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * Hardcore Alchemy Capstone is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Hardcore Alchemy Capstone.  If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.capstone.listener;

import java.util.Set;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import targoss.hardcorealchemy.listener.HardcoreAlchemyListener;
import targoss.hardcorealchemy.util.MobLists;

public class ListenerMobLists extends HardcoreAlchemyListener {
    @SubscribeEvent
    public void onAddMob(MobLists.AddEvent event) {
        switch(event.type) {
        case ENTITY_TAMEABLE:
            Set<String> entityTameables = event.set;
            
            // ToroQuest
            entityTameables.add("toroquest.toro");
            
            break;
        case GRASS:
            Set<String> grassMobs = event.set;
            
            // ToroQuest
            grassMobs.add("toroquest.toro");
            
            break;
        case HUMAN:
            Set<String> humans = event.set;
            
            // ToroQuest
            humans.add("toroquest.fugitive");
            humans.add("toroquest.guard");
            humans.add("toroquest.mage");
            humans.add("toroquest.rainbow_guard");
            humans.add("toroquest.rainbow_king");
            humans.add("toroquest.sentry");
            humans.add("toroquest.shopkeeper");
            humans.add("toroquest.village_lord");
            
            break;
        case LAND_ANIMAL:
            Set<String> landAnimals = event.set;
            
            // ToroQuest
            landAnimals.add("toroquest.toro");
            
            break;
        default:
            break;
        
        }
    }
}
