/**
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

package targoss.hardcorealchemy.capability.food;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import targoss.hardcorealchemy.HardcoreAlchemy;
import targoss.hardcorealchemy.capability.CapUtil;
import targoss.hardcorealchemy.util.MorphDiet;
import targoss.hardcorealchemy.util.MorphDiet.Restriction;

public class CapabilityFood implements ICapabilityFood {
    public static final ResourceLocation RESOURCE_LOCATION = new ResourceLocation(HardcoreAlchemy.MOD_ID, "food");
    
    @CapabilityInject(ICapabilityFood.class)
    public static final Capability<ICapabilityFood> FOOD_CAPABILITY = null;
    
    private MorphDiet.Restriction restriction;
    
    public static void register() {
        CapabilityManager.INSTANCE.register(ICapabilityFood.class, new StorageFood(), CapabilityFood.class);
        CapUtil.registerVirtualCapability(RESOURCE_LOCATION, FOOD_CAPABILITY);
    }
    
    public CapabilityFood() {
        this.restriction = null;
    }
    
    @Override
    public Restriction getRestriction() {
        return this.restriction;
    }

    @Override
    public void setRestriction(Restriction restriction) {
        this.restriction = restriction;
    }

}
