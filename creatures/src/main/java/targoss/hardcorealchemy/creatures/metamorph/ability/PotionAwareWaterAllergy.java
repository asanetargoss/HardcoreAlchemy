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

package targoss.hardcorealchemy.creatures.metamorph.ability;

import mchorse.vanilla_pack.abilities.WaterAllergy;
import net.minecraft.entity.EntityLivingBase;
import targoss.hardcorealchemy.creatures.item.Items;

public class PotionAwareWaterAllergy extends WaterAllergy {
    protected WaterAllergy delegate;
    
    public PotionAwareWaterAllergy(WaterAllergy delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void update(EntityLivingBase target) {
        if (!target.isPotionActive(Items.POTION_WATER_RESISTANCE)) {
            delegate.update(target);
        }
    }
}
