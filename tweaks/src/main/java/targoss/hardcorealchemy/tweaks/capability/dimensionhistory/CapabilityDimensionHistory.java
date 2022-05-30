/*
 * Copyright 2017-2022 asanetargoss
 *
 * This file is part of Hardcore Alchemy Tweaks.
 *
 * Hardcore Alchemy Tweaks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 3 or, at
 * your option, any later version of the GNU Lesser General Public License as
 * published by the Free Software Foundation.
 *
 * Hardcore Alchemy Tweaks is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along
 * with Hardcore Alchemy Tweaks. If not, see <http://www.gnu.org/licenses/>.
 */

package targoss.hardcorealchemy.tweaks.capability.dimensionhistory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class CapabilityDimensionHistory implements ICapabilityDimensionHistory {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "dimension_history");
    
    protected List<Integer> dimensionHistory = new ArrayList<>();

    @Override
    public List<Integer> getDimensionHistory() {
        return dimensionHistory;
    }

    @Override
    public void setDimensionHistory(List<Integer> dimensionHistory) {
        this.dimensionHistory = dimensionHistory;
    }

}
