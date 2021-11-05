/*
 * Copyright 2017-2018 asanetargoss
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

package targoss.hardcorealchemy.capability.killcount;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class CapabilityKillCount implements ICapabilityKillCount {
    
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "kill_count");
    
    public Map<String, Integer> killCounts = Collections.synchronizedMap(new HashMap<String, Integer>());
    
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

}
