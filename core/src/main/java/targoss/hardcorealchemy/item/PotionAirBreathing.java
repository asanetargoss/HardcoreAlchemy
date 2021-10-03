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

package targoss.hardcorealchemy.item;

import mchorse.metamorph.capabilities.morphing.IMorphing;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import targoss.hardcorealchemy.util.Color;

/**
 * This effect sets the player's morph's squid air to max every tick,
 * allowing players in water breathing morphs to breathe out of water.
 */
public class PotionAirBreathing extends HcAPotion {
    @CapabilityInject(IMorphing.class)
    protected static final Capability<IMorphing> MORPHING_CAPABILITY = null;
    
    public PotionAirBreathing(boolean isBadEffect, Color color, int iconId, boolean halfPixelOffsetRight) {
        super(isBadEffect, color, iconId, halfPixelOffsetRight);
    }
    
    @Override
    public void performEffect(EntityLivingBase entity, int ampifier) {
        IMorphing morphing = entity.getCapability(MORPHING_CAPABILITY, null);
        if (morphing != null) {
            morphing.setSquidAir(300);
        }
        // Squids will override their air meter if their air is set here, so the code for that is in a listener instead.
    }
}
