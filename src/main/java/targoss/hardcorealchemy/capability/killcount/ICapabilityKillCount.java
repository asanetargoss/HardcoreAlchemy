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

import java.util.Map;

public interface ICapabilityKillCount {
    /**
     * Given the name of an entity morph, get the
     * count of kills associated with the corresponding entity.
     */
    public abstract int getNumKills(String morphName);
    /**
     * Given the name of an entity morph, increment the
     * count of kills associated with the corresponding entity.
     */
    public abstract void addKill(String morphName);
    /**
     * Gets map (not a copy) between entity morph
     * names and the number of kills of mobs representing that morph.
     */
    public abstract Map<String, Integer> getKillCounts();
    /**
     * Sets map between entity morph names and the number of
     * kills of mobs representing that morph.
     */
    public abstract void setKillCounts(Map<String, Integer> killCounts);
}
