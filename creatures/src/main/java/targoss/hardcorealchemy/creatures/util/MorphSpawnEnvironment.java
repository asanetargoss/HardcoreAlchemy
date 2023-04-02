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

package targoss.hardcorealchemy.creatures.util;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import targoss.hardcorealchemy.ModState;

public class MorphSpawnEnvironment {
    private static Map<String, String> proxies = new HashMap<>();
    
    static {
        proxies.put("Blaze", "LavaSlime");
        String tc = ModState.THAUMCRAFT_ID + ".";
        proxies.put(tc + "ThaumSlime", tc + "Taintacle");
        proxies.put(tc + "TaintCrawler", tc + "Taintacle");
        proxies.put(tc + "TaintacleTiny", tc + "Taintacle");
        proxies.put(tc + "TaintSeed", tc + "Taintacle");
        proxies.put(tc + "TaintSeedPrime", tc + "Taintacle");
    }
    
    /**
     * Used by InstinctNeedSpawnEnvironment to determine what environment a player's
     * permanent morph wants to be in, based on the spawn conditions of the morph entity.
     * 
     * Given an entity, constructs an entity that should be used in its place for
     * performing this check. May be null, in which case just use the original entity.
     */
    public static @Nullable EntityLivingBase getSpawnCheckEntity(EntityLivingBase originalEntity) {
        String entityID = EntityList.getEntityString(originalEntity);
        if (entityID != null) {
            String proxyEntityID = proxies.get(entityID);
            if (proxyEntityID != null) {
                Entity proxyEntity = EntityList.createEntityByName(proxyEntityID, originalEntity.world);
                if (proxyEntity instanceof EntityLivingBase) {
                    return (EntityLivingBase)proxyEntity;
                }
            }
        }
        return null;
    }
}
