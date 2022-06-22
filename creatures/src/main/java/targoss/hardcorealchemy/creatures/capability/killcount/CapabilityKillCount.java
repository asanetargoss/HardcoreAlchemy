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

package targoss.hardcorealchemy.creatures.capability.killcount;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

public class CapabilityKillCount implements ICapabilityKillCount {
    
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "kill_count");
    
    public Map<String, Integer> killCounts = new HashMap<String, Integer>();
    public Set<String> masteredKills = new HashSet<>();
    
    @Override
    public int getNumKills(String morphName) {
        Integer kills = killCounts.get(morphName);
        if (kills != null) {
            return kills;
        }
        killCounts.put(morphName, 0);
        return 0;
    }

    @Override
    public void addKill(String morphName) {
        Integer kills = killCounts.get(morphName);
        if (kills != null) {
            killCounts.put(morphName, kills + 1);
        }
        else {
            killCounts.put(morphName, 1);
        }
    }

    @Override
    public Map<String, Integer> getKillCounts() {
        return killCounts;
    }

    @Override
    public void setKillCounts(Map<String, Integer> killCounts) {
        this.killCounts = killCounts;
    }

    @Override
    public Set<String> getMasteredKills() {
        return masteredKills;
    }

    @Override
    public void setMasteredKills(Set<String> masteredKills) {
        this.masteredKills = masteredKills;
    }

    @Override
    public boolean hasMasteredKill(String morphName) {
        return masteredKills.contains(morphName);
    }

    @Override
    public void addMasteredKill(String morphName) {
        masteredKills.add(morphName);
    }

}
