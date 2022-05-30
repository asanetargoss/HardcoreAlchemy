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

package targoss.hardcorealchemy.creatures.capability.morphstate;

import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemyCore;

public class CapabilityMorphState implements ICapabilityMorphState {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemyCore.MOD_ID, "morph_state");
    
    private boolean isFishingUnderwater = false;
    private int fishingTimer = 0;

    @Override
    public void setIsFishingUnderwater(boolean isFishingUnderwater) {
        this.isFishingUnderwater = isFishingUnderwater;
    }

    @Override
    public void setFishingTimer(int fishingTimer) {
        this.fishingTimer = fishingTimer;
    }

    @Override
    public boolean getIsFishingUnderwater() {
        return this.isFishingUnderwater;
    }

    @Override
    public int getFishingTimer() {
        return this.fishingTimer;
    }

}
