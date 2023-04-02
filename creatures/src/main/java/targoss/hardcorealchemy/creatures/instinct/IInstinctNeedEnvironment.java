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

package targoss.hardcorealchemy.creatures.instinct;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import targoss.hardcorealchemy.creatures.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.creatures.instinct.api.IInstinctState.NeedStatus;
import targoss.hardcorealchemy.creatures.instinct.api.InstinctNeedFactory;
import targoss.hardcorealchemy.util.MobLists;

/**
 * An instinct need to be in certain environmental conditions
 */
public interface IInstinctNeedEnvironment extends IInstinctNeed {
    public static class Factory extends InstinctNeedFactory {
        private boolean mobCacheInitialized = false;
        private Set<String> grassMobs = new HashSet<>();
        private Set<String> netherMobs = new HashSet<>();
        
        protected void initMobCache() {
            if (mobCacheInitialized) {
                return;
            }
            mobCacheInitialized = true;
            grassMobs = MobLists.getGrassMobs();
            netherMobs = MobLists.getNetherMobs();
        }
        
        @Override
        public IInstinctNeed createNeed(EntityLivingBase morphEntity) {
            initMobCache();
            String entityName = EntityList.getEntityString(morphEntity);
            if (grassMobs.contains(entityName)) {
                return new InstinctNeedForestPlains(morphEntity);
            }
            if (netherMobs.contains(entityName)) {
                return new InstinctNeedNether(morphEntity);
            }
            return new InstinctNeedSpawnEnvironment(morphEntity);
        }
    }

    /**
     * Calculates how well-suited the player is to their current environment.
     * Potentially called every tick.
     * */
    boolean doesPlayerFeelAtHome(EntityPlayer player, @Nullable EntityLivingBase morphEntity);
    
    public boolean doesReallyFeelAtHome();

    public boolean doesReallyNotFeelAtHome();

    ITextComponent getFeelsAtHomeMessage(NeedStatus needStatus);
    
    ITextComponent getNotAtHomeMessage(NeedStatus needStatus);
}
