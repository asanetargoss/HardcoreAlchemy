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

package targoss.hardcorealchemy.util;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

// TODO: Much of this happens at class load time, so need to be more careful
/**
 * Various mob lists, mainly used for deciding which
 * mobs should have certain tweaks. Each received list
 * is a fresh copy. Each member in these lists
 * is the entity id string.
 *
 */
public class MobLists {

    public static enum Type {
        BOSS,
        NON_MOB,
        HUMAN,
        HUMANOID,
        PASSIVE,
        GRASS,
        LAND_ANIMAL,
        ENTITY_TAMEABLE,
        NIGHT,
        NETHER,
        AURA,
        TAINT,
        ELDRITCH,
        TROLL,
        FREEBIE,
        WATER_ALLERGY, // TODO: Populate list
        ENDER_WATER_ALLERGY; // TODO: Populate list
        
        public Set<String> get() {
            Set<String> list = new HashSet<>();
            
            MinecraftForge.EVENT_BUS.post(new AddEvent(this, list));
            
            return list;
        }
    }
    
    public static class AddEvent extends Event {
        public final MobLists.Type type;
        public Set<String> set;
        
        public AddEvent(MobLists.Type type, Set<String> list) {
            this.type = type;
            this.set = list;
        }
    }

    public static Set<String> getBosses() {
        return Type.BOSS.get();
    }

    public static Set<String> getNonMobs() {
        return Type.NON_MOB.get();
    }

    public static Set<String> getHumans() {
        return Type.HUMAN.get();
    }
    
    public static Set<String> getHumanoids() {
        return Type.HUMANOID.get();
    }

    public static Set<String> getPassiveMobs() {
        return Type.PASSIVE.get();
    }
    
    /**
     * Mobs that like spawning on grass during the day
     */
    public static Set<String> getGrassMobs() {
        return Type.GRASS.get();
    }
    
    /**
     * Mobs that are considered natural animals and spawn in a certain location on land
     */
    public static Set<String> getLandAnimals() {
        return Type.LAND_ANIMAL.get();
    }

    /*
     * All classes of this set must be derived from EntityTameable
     */
    public static Set<String> getEntityTameables() {
        return Type.ENTITY_TAMEABLE.get();
    }

    /**
     * Overworld mobs which spawn in darkness
     */
    public static Set<String> getNightMobs() {
        return Type.NIGHT.get();
    }

    public static Set<String> getNetherMobs() {
        return Type.NETHER.get();
    }

    public static Set<String> getAuraMobs() {
        return Type.AURA.get();
    }

    public static Set<String> getTaintMobs() {
        return Type.TAINT.get();
    }

    public static Set<String> getEldritchMobs() {
        return Type.ELDRITCH.get();
    }

    public static Set<String> getTrollMobs() {
        return Type.TROLL.get();
    }

    /**
     * Morphs of these mobs do not increase max humanity
     * when acquired.
     */
    public static Set<String> getFreebieMobs() {
        return Type.FREEBIE.get();
    }

}
