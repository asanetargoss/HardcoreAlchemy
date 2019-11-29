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

package targoss.hardcorealchemy.capability.misc;

import java.util.UUID;

import net.minecraft.util.ResourceLocation;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class CapabilityMisc implements ICapabilityMisc {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "misc");
    
    private boolean hasSeenThirstWarning = false;
    private boolean hasSeenMagicInhibitionWarning = false;
    private UUID lifetimeUUID = null;
    
    @Override
    public boolean getHasSeenThirstWarning() {
        return hasSeenThirstWarning;
    }

    @Override
    public boolean getHasSeenMagicInhibitionWarning() {
        return hasSeenMagicInhibitionWarning;
    }

    @Override
    public void setHasSeenMagicInhibitionWarning(boolean hasSeenMagicInhibitionWarning) {
        this.hasSeenMagicInhibitionWarning = hasSeenMagicInhibitionWarning;
    }

    @Override
    public void setHasSeenThirstWarning(boolean hasSeenThirstWarning) {
        this.hasSeenThirstWarning = hasSeenThirstWarning;
    }

    @Override
    public UUID getLifetimeUUID() {
        return lifetimeUUID;
    }

    @Override
    public void setLifetimeUUID(UUID uuid) {
        this.lifetimeUUID = uuid;
    }
}
