/*
 * Copyright 2019 asanetargoss
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

package targoss.hardcorealchemy.instinct;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import targoss.hardcorealchemy.instinct.api.IInstinctNeed;
import targoss.hardcorealchemy.instinct.api.IInstinctState.NeedStatus;
import targoss.hardcorealchemy.instinct.api.InstinctNeedFactory;
import targoss.hardcorealchemy.util.EntityUtil;
import targoss.hardcorealchemy.util.MobLists;

/**
 * An instinct need to be in certain environmental conditions
 */
public interface IInstinctNeedEnvironment extends IInstinctNeed {
    public static class Factory extends InstinctNeedFactory {
        private List<Class<? extends EntityLivingBase>> grassMobs = null;
        
        @SuppressWarnings("unchecked")
        protected void initMobCache() {
            if (grassMobs != null) {
                return;
            }
            for (String grassMobName : MobLists.getGrassMobs()) {
                if (!EntityUtil.isValidEntityName(grassMobName)) {
                    continue;
                }
                grassMobs.add((Class<? extends EntityLivingBase>)EntityList.getClassFromID(EntityList.getIDFromString(grassMobName)));
            }
            // TODO: Nether mobs once those are included
        }
        
        @Override
        public IInstinctNeed createNeed(EntityLivingBase morphEntity) {
            initMobCache();
            for (Class<? extends EntityLivingBase> grassMobType : grassMobs) {
                if (grassMobType.isInstance(morphEntity)) {
                    return new InstinctNeedForestPlains(morphEntity);
                }
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
