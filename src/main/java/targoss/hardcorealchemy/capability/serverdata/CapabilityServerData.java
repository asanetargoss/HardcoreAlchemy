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

package targoss.hardcorealchemy.capability.serverdata;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.CapabilityManager;
import targoss.hardcorealchemy.HardcoreAlchemy;

public class CapabilityServerData implements ICapabilityServerData {
    
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "serverData");
    
    private boolean hasDifficulty;
    
    public static void register() {
        CapabilityManager.INSTANCE.register(ICapabilityServerData.class, new StorageServerData(), CapabilityServerData.class);
    }

    @Override
    public void setHasDifficulty(boolean hasDifficulty) {
        this.hasDifficulty = hasDifficulty;
    }

    @Override
    public boolean getHasDifficulty() {
        return hasDifficulty;
    }

}
