/*
 * Copyright 2018 asanetargoss
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

package targoss.hardcorealchemy.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIWander;

/**
 * Wander AI which is active during the activeTime tick interval and
 * inactive during the inactiveTime tick interval.
 * Give this a high AI priority.
 */
public class AIWanderWithTiredness extends EntityAIWander {
    public int activityTick = 0;
    private final int activeTime;
    private final int inactiveTime;

    public AIWanderWithTiredness(EntityCreature creature, double speed, int wanderChance, int activeTime, int inactiveTime) {
        super(creature, speed, wanderChance);
        this.activeTime = activeTime;
        this.inactiveTime = inactiveTime;
    }
    
    @Override
    public boolean shouldExecute() {
        if (++activityTick > activeTime+inactiveTime) activityTick = 0;
        
        if (!super.shouldExecute()) {
            return false;
        }
        
        return activityTick <= activeTime;
    }
    
    @Override
    public boolean continueExecuting() {
        if (++activityTick > activeTime+inactiveTime) activityTick = 0;
        
        if (!super.continueExecuting()) {
            return false;
        }
        
        return activityTick <= activeTime;
    }

}
